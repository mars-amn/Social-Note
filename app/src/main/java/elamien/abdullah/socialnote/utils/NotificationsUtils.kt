package elamien.abdullah.socialnote.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.text.HtmlCompat
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
import elamien.abdullah.socialnote.ui.AddEditNoteActivity

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class NotificationsUtils {

    fun sendNotification(context : Context, intent : Intent) {
        val notificationManager : NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                context.getString(R.string.notification_id),
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = getNotificationBuilder(context, intent)

        notificationManager.notify(
            context.resources.getInteger(R.integer.note_reminder_notification_id),
            builder?.build()
        )
    }

    private fun getNotificationBuilder(context : Context,
                                       intent : Intent) : NotificationCompat.Builder? {
        return NotificationCompat.Builder(context, context.getString(R.string.notification_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.note_notification_title))
            .setContentText(
                getBody(intent.getStringExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY)!!)
            )
            .setContentIntent(getOpenNoteNotificationPendingIntent(context, intent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(getOpenNoteAction(context, intent))
            .addAction(dismissNotificationsAction(context))
    }

    @Suppress("DEPRECATION")
    private fun getBody(body : String) : Spanned {
        val editedBody = body.substring(0, body.lastIndex / 2)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            HtmlCompat.fromHtml(
                "$editedBody ...", Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            Html.fromHtml(editedBody)
        }
    }

    private fun getOpenNoteAction(context : Context, intent : Intent) : NotificationCompat.Action {
        return NotificationCompat.Action(
            R.drawable.ic_notification_open,
            context.getString(R.string.note_notification_open_action_label),
            getOpenNoteNotificationPendingIntent(context, intent)
        )
    }

    private fun dismissNotificationsAction(context : Context) : NotificationCompat.Action {
        return NotificationCompat.Action(
            R.drawable.ic_dismiss_notification_action,
            context.getString(R.string.note_notification_dismiss_action_label),
            getDismissNotificationPendingIntent(context)
        )
    }

    private fun getOpenNoteNotificationPendingIntent(context : Context,
                                                     intent : Intent) : PendingIntent? {
        val noteIntent = Intent(context, AddEditNoteActivity::class.java)
        noteIntent.putExtra(Constants.ACTIVITY_NOTIFICATION_OPEN, true)
        noteIntent.putExtra(Constants.NOTE_INTENT_KEY, intent.getLongExtra(Constants.NOTE_INTENT_ID, -1))
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(noteIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun getDismissNotificationPendingIntent(context : Context) : PendingIntent? {
        val dismissIntent = Intent(context, NoteReminderReceiver::class.java)
        dismissIntent.action = Constants.DISMISS_NOTIFICATION_ACTION
        return PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun dismissNoteReminderNotification(context : Context) {
        val notificationManager : NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(context.resources.getInteger(R.integer.note_reminder_notification_id))
    }

    /**
     * I am just gonna leave it here
     * I am sure I'll use it later somehow
     * I hope.
     */
    fun dismissAllNotifications(context : Context) {
        val notificationManager : NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    companion object {
        fun getNotificationUtils() : NotificationsUtils {
            return NotificationsUtils()
        }
    }
}