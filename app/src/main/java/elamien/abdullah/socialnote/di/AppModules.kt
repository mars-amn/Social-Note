package elamien.abdullah.socialnote.di

import androidx.room.Room
import elamien.abdullah.socialnote.database.NotesDatabase
import elamien.abdullah.socialnote.repository.NoteRepository
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
val appModules = module {
    single { Room.databaseBuilder(androidContext(), NotesDatabase::class.java, "notes.db").build() }
    single { get<NotesDatabase>().notesDao() }
    single { NoteRepository() }
    viewModel { NoteViewModel() }
}