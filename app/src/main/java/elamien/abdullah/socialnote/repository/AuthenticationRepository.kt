package elamien.abdullah.socialnote.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import elamien.abdullah.socialnote.eventbus.AuthenticationEvent
import elamien.abdullah.socialnote.utils.Constants
import org.greenrobot.eventbus.EventBus
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class AuthenticationRepository : IAuthenticationRepository, KoinComponent {
    private val mAuth by inject<FirebaseAuth>()

    override fun registerGoogleUser(task : Task<GoogleSignInAccount>) {
        try {
            val account = task.result
            authWithFirebase(account)
        } catch (e : ApiException) {
            postAuthEvent(Constants.AUTH_EVENT_FAIL)
            e.printStackTrace()
        }

    }

    private fun authWithFirebase(account : GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    postAuthEvent(Constants.AUTH_EVENT_SUCCESS)
                } else {
                    postAuthEvent(Constants.AUTH_EVENT_FAIL)
                }
            }
    }

    private fun postAuthEvent(event : String) {
        EventBus.getDefault().post(AuthenticationEvent(event))
    }
}