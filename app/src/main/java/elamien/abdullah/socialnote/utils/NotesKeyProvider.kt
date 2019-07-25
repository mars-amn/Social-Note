package elamien.abdullah.socialnote.utils

import android.util.Log
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.adapter.PagedNoteListAdapter

/**
 * Created by AbdullahAtta on 7/25/2019.
 */
class NotesKeyProvider(private val recyclerView : RecyclerView, private val adapter : PagedNoteListAdapter) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position : Int) : Long? {
        Log.d("select getKey", adapter.getNoteId(position).toString())
        return adapter.getNoteId(position)
    }

    override fun getPosition(key : Long) : Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}