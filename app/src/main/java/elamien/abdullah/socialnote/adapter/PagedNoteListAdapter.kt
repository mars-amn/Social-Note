package elamien.abdullah.socialnote.adapter

import android.animation.TimeInterpolator
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.transitionseverywhere.extra.Scale
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ListItemNotesBinding
import elamien.abdullah.socialnote.ui.AddNoteActivity
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.utils.NotesDiffCallback


/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class PagedNoteListAdapter(private val context : Context) :
    PagedListAdapter<Note, PagedNoteListAdapter.NotesViewHolder>(NotesDiffCallback()) {
    var tracker : SelectionTracker<Long>? = null

    override fun getItemId(position : Int) : Long = getNoteId(position)!!

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : NotesViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemNotesBinding.inflate(inflater, parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder : NotesViewHolder, position : Int) {
        tracker?.let {
            holder.bind(getItem(position), it.isSelected(getNoteId(position)))
        }

    }

    fun getNoteId(position : Int) : Long? {
        return getItem(position)?.id
    }

    inner class NotesViewHolder(var mBinding : ListItemNotesBinding) : RecyclerView.ViewHolder(mBinding.root) {
        init {
            mBinding.handlers = this
        }

        fun getItemDetails() : ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition() : Int = adapterPosition
                override fun getSelectionKey() : Long? = getNoteId(adapterPosition)
            }

        fun bind(note : Note?, isActivated : Boolean = false) {
            mBinding.note = note
            mBinding.root.isActivated = isActivated

            if (isActivated) {
                setTransition(FastOutLinearInInterpolator())
                mBinding.selectionIcon.visibility = View.VISIBLE

            } else {
                mBinding.selectionIcon.visibility = View.GONE
                setTransition(LinearOutSlowInInterpolator())
            }
        }

        private fun setTransition(interpolator : TimeInterpolator?) {
            val set = TransitionSet()
                .addTransition(Scale(0.9f))
                .addTransition(Fade())
                .setInterpolator(interpolator)
            TransitionManager.beginDelayedTransition(mBinding.parent, set)
        }

        fun onNoteClick(view : View) {
            val noteIntent = Intent(context, AddNoteActivity::class.java)
            noteIntent.putExtra(Constants.NOTE_INTENT_KEY, getItem(adapterPosition)?.id)
            context.startActivity(noteIntent)
        }
    }
}