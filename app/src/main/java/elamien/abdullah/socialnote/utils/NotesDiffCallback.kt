package elamien.abdullah.socialnote.utils

import androidx.recyclerview.widget.DiffUtil
import elamien.abdullah.socialnote.database.local.notes.Note

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class NotesDiffCallback : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }

}