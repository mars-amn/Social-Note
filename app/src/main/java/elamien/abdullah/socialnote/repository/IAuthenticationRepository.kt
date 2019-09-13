package elamien.abdullah.socialnote.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
interface IAuthenticationRepository {

    fun registerGoogleUser(task: Task<GoogleSignInAccount>)
    fun registerFacebookUser(credential: AuthCredential)
}