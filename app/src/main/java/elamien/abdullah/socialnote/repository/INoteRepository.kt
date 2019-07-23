package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import elamien.abdullah.socialnote.database.Note

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
interface INoteRepository {
    fun insertNote(note: Note): LiveData<Long>
    fun dispose()
}