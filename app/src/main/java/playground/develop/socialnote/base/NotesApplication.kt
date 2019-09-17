package playground.develop.socialnote.base

import android.app.Application
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import playground.develop.socialnote.di.appModules
import playground.develop.socialnote.di.firebaseModules
import playground.develop.socialnote.di.repositoriesModules
import playground.develop.socialnote.di.viewModelsModules

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class NotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        startKoin {
            androidContext(this@NotesApplication)
            androidLogger(Level.DEBUG)
            modules(listOf(appModules, viewModelsModules, repositoriesModules, firebaseModules))
        }
    }
}