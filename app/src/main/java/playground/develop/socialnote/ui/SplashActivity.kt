package playground.develop.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import playground.develop.socialnote.R
import playground.develop.socialnote.utils.Constants.Companion.APP_PREFERENCE_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRST_LAUNCH_KEY
import playground.develop.socialnote.utils.Constants.Companion.SKIP_REGISTER_KEY

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        setContentView(R.layout.activity_splash)

        if (!isUserFirstLaunch) {
            startOnBoardingActivity()
        } else if (!isUserSkipRegister) {
            startRegisterActivity()
        } else {
            startHomeActivity()
        }
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startOnBoardingActivity() {
        val intent = Intent(this, OnBoardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val isUserSkipRegister: Boolean
        get() {
            val preferences = getSharedPreferences(APP_PREFERENCE_NAME, MODE_PRIVATE)
            return preferences.getBoolean(SKIP_REGISTER_KEY, false)
        }
    private val isUserFirstLaunch: Boolean
        get() {
            val preferences = getSharedPreferences(APP_PREFERENCE_NAME, MODE_PRIVATE)
            return preferences.getBoolean(FIRST_LAUNCH_KEY, false)
        }

    private fun setupFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

}
