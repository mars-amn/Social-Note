package elamien.abdullah.socialnote.base

import android.app.Application
import com.google.firebase.FirebaseApp
import elamien.abdullah.socialnote.di.appModules
import elamien.abdullah.socialnote.di.firebaseModules
import elamien.abdullah.socialnote.di.repositoriesModules
import elamien.abdullah.socialnote.di.viewModelsModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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