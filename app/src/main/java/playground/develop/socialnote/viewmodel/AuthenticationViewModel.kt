package playground.develop.socialnote.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import org.koin.core.KoinComponent
import org.koin.core.inject
import playground.develop.socialnote.repository.AuthenticationRepository

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class AuthenticationViewModel : ViewModel(), KoinComponent {

    private val mAuthRepository: AuthenticationRepository by inject()
    fun registerGoogleUser(task: Task<GoogleSignInAccount>) {
        return mAuthRepository.registerGoogleUser(task)
    }

    fun registerFacebookUser(credential: AuthCredential) {
        mAuthRepository.registerFacebookUser(credential)
    }

    fun loginTwitterUser(result: AuthResult) {
        mAuthRepository.loginTwitterUser(result)
    }

}