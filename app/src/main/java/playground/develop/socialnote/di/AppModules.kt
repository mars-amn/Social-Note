package playground.develop.socialnote.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import playground.develop.socialnote.database.local.AppDatabase
import playground.develop.socialnote.repository.AuthenticationRepository
import playground.develop.socialnote.repository.NoteRepository
import playground.develop.socialnote.repository.PostRepository
import playground.develop.socialnote.viewmodel.AuthenticationViewModel
import playground.develop.socialnote.viewmodel.NoteViewModel
import playground.develop.socialnote.viewmodel.PostViewModel

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