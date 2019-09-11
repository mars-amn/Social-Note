package elamien.abdullah.socialnote.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import elamien.abdullah.socialnote.database.local.AppDatabase
import elamien.abdullah.socialnote.repository.AuthenticationRepository
import elamien.abdullah.socialnote.repository.NoteRepository
import elamien.abdullah.socialnote.repository.PostRepository
import elamien.abdullah.socialnote.viewmodel.AuthenticationViewModel
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
val appModules = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "notes.db")
            .build()
    }

    single { get<AppDatabase>().notesDao() }
}
val repositoriesModules = module {
    single { NoteRepository() }
    single { AuthenticationRepository() }
    single { PostRepository() }
}
val viewModelsModules = module {
    viewModel { NoteViewModel() }
    viewModel { AuthenticationViewModel() }
    viewModel { PostViewModel() }
}

val firebaseModules = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
}