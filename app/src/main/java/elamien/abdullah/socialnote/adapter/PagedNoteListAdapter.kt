package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.local.notes.Note
import elamien.abdullah.socialnote.databinding.ListItemNotesBinding
import elamien.abdullah.socialnote.ui.AddEditNoteActivity
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.utils.NotesDiffCallback
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class PagedNoteListAdapter(
    private val listener: LongClickListener,
    private val context: Context
) :
    PagedListAdapter<Note, PagedNoteListAdapter.NotesViewHolder>(NotesDiffCallback()) {

    val backgroundColors = context.resources.getIntArray(R.array.recyclerViewBackgroundColors)
        .toCollection(ArrayList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemNotesBinding.inflate(inflater, parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    var previousRandomColor = 0
    var colorBeforePrevious = 1

    inner class NotesViewHolder(var mBinding: ListItemNotesBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.handlers = this
        }

        fun bind(note: Note?) {
            mBinding.note = note
            setNoteBackground()
        }

        private fun setNoteBackground() {
            var newRandomColor: Int
            do {
                newRandomColor = getRandomColor()
            } while (newRandomColor == previousRandomColor || newRandomColor == colorBeforePrevious)
            mBinding.listItemNoteParent.setBackgroundColor(newRandomColor)
            colorBeforePrevious = previousRandomColor
            previousRandomColor = newRandomColor
        }

        private fun getRandomColor() = backgroundColors[Random().nextInt(backgroundColors.size)]


        fun onNoteLongClick(view: View): Boolean {
            MaterialAlertDialogBuilder(context).setTitle(context.getString(R.string.delete_note_dialog_title))
                .setMessage(context.getString(R.string.delete_note_dialog_message))
                .setPositiveButton(context.getString(R.string.delete_note_dialog_positive_button_label)) { dialog, _ ->
                    deleteNote()
                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.delete_note_dialog_negative_button_label)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

            return true
        }

        private fun deleteNote() {
            listener.onLongClickListener(getItem(adapterPosition)!!)
        }

        fun onNoteClick(view: View) {
            val noteIntent = Intent(context, AddEditNoteActivity::class.java)
            noteIntent.putExtra(Constants.NOTE_INTENT_KEY, getItem(adapterPosition)?.id)
            context.startActivity(noteIntent)
        }
    }

    interface LongClickListener {
        fun onLongClickListener(note: Note)
    }
}