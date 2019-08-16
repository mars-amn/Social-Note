package elamien.abdullah.socialnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.utils.NotificationsUtils

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class NoteReminderReceiver : BroadcastReceiver() {

	override fun onReceive(context : Context?, intent : Intent?) {
		if (context != null && intent != null) {
			val intentAction = intent.action
			if (intentAction == Constants.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION) {
				dismissNotification(context)
			} else if (intentAction == Constants.NOTE_TIME_REMINDER_ACTION) {
				showNotification(context, intent)
			}
		}
	}

	private fun dismissNotification(context : Context) {
		NotificationsUtils.getNotificationUtils().dismissNoteReminderNotification(context)
	}

	private fun showNotification(context : Context?, intent : Intent) {
		NotificationsUtils.getNotificationUtils().sendNoteTimeReminderNotification(context!!, intent)
	}

}