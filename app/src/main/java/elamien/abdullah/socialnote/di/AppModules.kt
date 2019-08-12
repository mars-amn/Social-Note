package elamien.abdullah.socialnote.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import elamien.abdullah.socialnote.database.NotesDatabase
import elamien.abdullah.socialnote.repository.AuthenticationRepository
import elamien.abdullah.socialnote.repository.NoteRepository
import elamien.abdullah.socialnote.viewmodel.AuthenticationViewModel
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
val appModules = module {
    single { Room.databaseBuilder(androidContext(), NotesDatabase::class.java, "notes.db").build() }
    single { FirebaseAuth.getInstance() }
    single { get<NotesDatabase>().notesDao() }
    single { NoteRepository() }
    single { AuthenticationRepository() }
    viewModel { NoteViewModel() }
    viewModel { AuthenticationViewModel() }
}