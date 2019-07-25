package elamien.abdullah.socialnote.utils

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.adapter.PagedNoteListAdapter

/**
 * Created by AbdullahAtta on 7/25/2019.
 */
class NotesItemLookUp(private val recyclerView : RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event : MotionEvent) : ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as PagedNoteListAdapter.NotesViewHolder)
                .getItemDetails()
        }
        return null
    }
}