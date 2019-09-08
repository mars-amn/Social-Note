package elamien.abdullah.socialnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import elamien.abdullah.socialnote.database.local.notes.Note
import elamien.abdullah.socialnote.repository.NoteRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class NoteViewModel : ViewModel(), KoinComponent {

    private val mNoteRepository: NoteRepository by inject()
    fun insertNewNote(note: Note): LiveData<Long> {
        return mNoteRepository.insertNote(note)
    }

    fun loadPagedNotes(): LiveData<PagedList<Note>> {
        return mNoteRepository.loadPagedNotes()
    }

    fun getNote(noteId: Long): LiveData<Note> {
        return mNoteRepository.getNote(noteId)
    }

    fun updateNote(note: Note) {
        mNoteRepository.updateNote(note)
    }

    fun deleteNote(note: Note?) {
        mNoteRepository.deleteNote(note!!)
    }

    fun searchForNote(query: String): LiveData<PagedList<Note>> {
        return mNoteRepository.searchForNote(query)
    }

    override fun onCleared() {
        super.onCleared()
        mNoteRepository.dispose()
    }

}