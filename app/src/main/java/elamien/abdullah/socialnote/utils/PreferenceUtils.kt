package elamien.abdullah.socialnote.utils

import android.content.Context
import androidx.preference.PreferenceManager
import elamien.abdullah.socialnote.R

/**
 * Created by AbdullahAtta on 13-Sep-19.
 */
class PreferenceUtils {
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