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
        val preferences = context.getSharedPreferences(
            Constants.APP_PREFERENCE_NAME,
            AppCompatActivity.MODE_PRIVATE
        )

        return preferences.getString(
            Constants.USER_COUNTRY_ISO_KEY,
            Constants.USER_COUNTRY_ISO_ERROR_KEY
        )
    }

    fun disableNoteSync(context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(context.getString(R.string.note_sync_key), false)
        editor.apply()
    }

    fun enableNoteSync(context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(context.getString(R.string.note_sync_key), true)
        editor.apply()
    }

    fun isUserEnableSync(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(context.getString(R.string.note_sync_key), true)
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