package playground.develop.socialnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.NotificationsUtils

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class NoteReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val intentAction = intent.action
            if (intentAction == Constants.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION) {
                dismissNotification(context, intent.getLongExtra(Constants.NOTE_INTENT_KEY, -1))
            } else if (intentAction == Constants.NOTE_TIME_REMINDER_ACTION) {
                showNotification(
                    context,
                    intent.getStringExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY),
                    intent.getLongExtra(Constants.NOTE_INTENT_KEY, -1)
                )
            }
        }
    }

    private fun dismissNotification(context: Context, id: Long) {
        NotificationsUtils.getNotificationUtils()
            .dismissNoteReminderNotification(context, id.toInt())
    }

    private fun showNotification(context: Context?, noteBody: String?, noteId: Long) {
        NotificationsUtils.getNotificationUtils()
            .sendNoteTimeReminderNotification(context!!, noteBody!!, noteId)
    }

}