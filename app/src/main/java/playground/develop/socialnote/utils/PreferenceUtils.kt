package playground.develop.socialnote.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import playground.develop.socialnote.R

/**
 * Created by AbdullahAtta on 13-Sep-19.
 */
class PreferenceUtils {

    fun getUserCountryCode(context: Context): String? {
        val preferences = context.getSharedPreferences(Constants.APP_PREFERENCE_NAME,
                                                       AppCompatActivity.MODE_PRIVATE)

        return preferences.getString(Constants.USER_COUNTRY_ISO_KEY,
                                     Constants.USER_COUNTRY_ISO_ERROR_KEY)
    }

    fun isPushNotificationsEnabled(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(context.getString(R.string.push_notifications_key), true)
    }

    companion object {
        fun getPreferenceUtils(): PreferenceUtils {
            return PreferenceUtils()
        }
    }
}