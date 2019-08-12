package elamien.abdullah.socialnote.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
interface IAuthenticationRepository {
    fun registerGoogleUser(task : Task<GoogleSignInAccount>)
}