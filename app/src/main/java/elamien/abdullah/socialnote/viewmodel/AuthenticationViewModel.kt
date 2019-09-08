package elamien.abdullah.socialnote.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import elamien.abdullah.socialnote.repository.AuthenticationRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class AuthenticationViewModel : ViewModel(), KoinComponent {

    private val mAuthRepository: AuthenticationRepository by inject()
    fun registerGoogleUser(task: Task<GoogleSignInAccount>) {
        return mAuthRepository.registerGoogleUser(task)
    }
}