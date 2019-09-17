package playground.develop.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil.api.load
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.transitionseverywhere.extra.Scale
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import playground.develop.socialnote.R
import playground.develop.socialnote.databinding.ActivityRegisterBinding
import playground.develop.socialnote.eventbus.AuthenticationEvent
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.viewmodel.AuthenticationViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityRegisterBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val mAuthViewModel: AuthenticationViewModel by inject()
    val mFirebaseAuth: FirebaseAuth by inject()
    private var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        mBinding = DataBindingUtil.setContentView(this@RegisterActivity, R.layout.activity_register)
        mBinding.handlers = this
        registerEventBus()
        if (mFirebaseAuth.currentUser != null) {
            startHomeActivity()
        } else if (intent.hasExtra(Constants.CONSIDER_REGISTER_KEY) || !isUserSkipRegister) {
            setupRegisterScreen()
            setupFacebookRegister()
        } else if (isUserSkipRegister) {
            startHomeActivity()
        }
    }

    private val isUserSkipRegister: Boolean
        get() {
            val preferences = getSharedPreferences(Constants.APP_PREFERENCE_NAME, MODE_PRIVATE)
            return preferences.getBoolean(Constants.SKIP_REGISTER_KEY, false)
        }

    private fun setupFacebookRegister() {
        mCallbackManager = CallbackManager.Factory.create()
        mBinding.facebookLoginButton.setPermissions("email", "public_profile")
        mBinding.facebookLoginButton
                .registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        registerFacebookUser(loginResult.accessToken)
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException) {
                    }
                })
    }

    fun onFacebookButtonClick(view: View) {
        mBinding.facebookLoginButton.performClick()
    }


    fun onTwitterButtonClick(view: View) {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val pendingResultTask = mFirebaseAuth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener { authResult ->
                mAuthViewModel.loginTwitterUser(authResult)
                startHomeActivity()
            }.addOnFailureListener {
                Toast.makeText(this@RegisterActivity,
                               "Something went wrong! " + it.message,
                               Toast.LENGTH_SHORT).show()
            }
        } else {
            mFirebaseAuth
                    .startActivityForSignInWithProvider(this@RegisterActivity, provider.build())
                    .addOnSuccessListener { authResult ->
                        mAuthViewModel.loginTwitterUser(authResult)
                        startHomeActivity()
                    }.addOnFailureListener {
                        Toast.makeText(this@RegisterActivity,
                                       "Something went wrong! " + it.message,
                                       Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun registerFacebookUser(token: AccessToken) {
        mAuthViewModel.registerFacebookUser(FacebookAuthProvider.getCredential(token.token))
    }

    private fun registerEventBus() {
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        unregisterEventBus()
    }

    private fun unregisterEventBus() {
        EventBus.getDefault().unregister(this)
    }

    private fun setupFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun onGoogleClick(view: View) {
        mGoogleSignInClient = GoogleSignIn.getClient(this, getSignInOptions()!!)
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    private fun getSignInOptions(): GoogleSignInOptions? {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_api_key)).requestEmail().build()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE && resultCode == RESULT_OK) {
            mAuthViewModel.registerGoogleUser(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

    @Subscribe
    fun onEvent(event: AuthenticationEvent) {
        if (event.authenticationEventMessage == Constants.AUTH_EVENT_SUCCESS) {
            if (callingActivity == null) {
                startHomeActivity()
            } else {
                setResult(RESULT_OK)
                finish()
            }
        } else if (event.authenticationEventMessage == Constants.AUTH_EVENT_FAIL) {
            Toast.makeText(this@RegisterActivity,
                           getString(R.string.auth_failed_msg),
                           Toast.LENGTH_LONG).show()
        }
    }

    fun onSkipRegistrationClick(view: View) {
        if (callingActivity == null) {
            saveUserSkipRegister()
            startHomeActivity()
        } else if (callingActivity != null) {
            saveUserSkipRegister()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun startHomeActivity() {
        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupRegisterScreen() {
        applyAnimation()
        mBinding.registerImageBackground.load(R.drawable.register_background) {
            crossfade(true)
        }
        mBinding.registerGroup.visibility = View.VISIBLE
    }

    private fun applyAnimation() {
        val set = TransitionSet().addTransition(Scale(0.7f)).addTransition(Fade())
                .setInterpolator(FastOutLinearInInterpolator())
        TransitionManager.beginDelayedTransition(mBinding.parent, set)
    }

    private fun saveUserSkipRegister() {
        val editor = getSharedPreferences(Constants.APP_PREFERENCE_NAME, MODE_PRIVATE).edit()
        editor.putBoolean(Constants.SKIP_REGISTER_KEY, true)
        editor.apply()
    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1
    }
}
