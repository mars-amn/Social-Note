package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import elamien.abdullah.socialnote.database.local.notes.Note

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
interface INoteRepository {

	fun insertNote(note : Note) : LiveData<Long>
	fun dispose()
	fun loadPagedNotes() : LiveData<PagedList<Note>>
	fun getNote(noteId : Long) : LiveData<Note>
	fun updateNote(note : Note)
	fun deleteNote(note : Note)
	fun searchForNote(query : String) : LiveData<PagedList<Note>>
	fun deleteAllRows()
	fun getAllNoteGeofences() : List<Note>
}