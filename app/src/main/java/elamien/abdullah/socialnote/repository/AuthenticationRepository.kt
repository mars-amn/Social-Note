package elamien.abdullah.socialnote.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import elamien.abdullah.socialnote.eventbus.AuthenticationEvent
import elamien.abdullah.socialnote.utils.Constants
import org.greenrobot.eventbus.EventBus
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class AuthenticationRepository : IAuthenticationRepository, KoinComponent {

    private val mAuth by inject<FirebaseAuth>()
    private val mFirestore by inject<FirebaseFirestore>()

    override fun registerGoogleUser(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.result
            authWithFirebase(account)
        } catch (e: ApiException) {
            postAuthEvent(Constants.AUTH_EVENT_FAIL)
            e.printStackTrace()
        }

    }

    private fun authWithFirebase(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.additionalUserInfo?.isNewUser!!) {
                        addNewUserToFirestore(task.result?.user!!)
                    }
                    postAuthEvent(Constants.AUTH_EVENT_SUCCESS)
                } else {
                    postAuthEvent(Constants.AUTH_EVENT_FAIL)
                }
            }
    }

    private fun addNewUserToFirestore(user: FirebaseUser) {
        mFirestore.collection(Constants.FIRESTORE_USERS_COLLECTION_NAME)
            .document(user.uid)
            .set(getMappedUser(user))
    }

    private fun getMappedUser(user: FirebaseUser): HashMap<String, Any> {
        val userMap = HashMap<String, Any>()
        userMap[Constants.FIRESTORE_USER_UID] = user.uid
        userMap[Constants.FIRESTORE_USER_IMAGE_URL] = user.photoUrl.toString()
        userMap[Constants.FIRESTORE_USER_NAME] = user.displayName!!
        userMap[Constants.FIRESTORE_USER_TITLE] = "Reader"
        userMap[Constants.FIRESTORE_USER_POSTS_COUNT] = 0
        userMap[Constants.FIRESTORE_USER_COVER_IMAGE] = getRandomImage()
        return userMap
    }

    private fun postAuthEvent(event: String) {
        EventBus.getDefault()
            .post(AuthenticationEvent(event))
    }

    private fun getRandomImage() = images[Random().nextInt(images.size)]

    private val images = arrayOf(
        "http://bit.ly/2PhvwfN",
        "http://bit.ly/2HpJ2aH",
        "http://bit.ly/327HLNz",
        "http://bit.ly/2Pd352y",
        "http://bit.ly/2MFbiKO",
        "http://bit.ly/341m7wh",
        "http://bit.ly/2U6i0dL",
        "http://bit.ly/2KZJ5fy"
    )
}