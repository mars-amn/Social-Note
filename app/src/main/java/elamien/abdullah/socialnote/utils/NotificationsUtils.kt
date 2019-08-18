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
import elamien.abdullah.socialnote.receiver.GeofenceReminderReceiver
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
import elamien.abdullah.socialnote.ui.AddEditNoteActivity

/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class NotificationsUtils {

	fun sendNoteGeofenceReminderNotification(context : Context, noteBody : String, noteId : Long) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(context.getString(R.string.note_geofence_notification_id),
					context.getString(R.string.note_geofence_notification_name),
					NotificationManager.IMPORTANCE_HIGH)
			notificationManager.createNotificationChannel(channel)
		}

		val builder = getGeofenceNoteNotificationBuilder(context, noteBody, noteId)

		notificationManager.notify(context.resources.getInteger(R.integer.note_geofence_reminder_notification_id) + noteId.toInt(),
				builder?.build())
	}

	private fun getGeofenceNoteNotificationBuilder(context : Context,
												   noteBody : String,
												   noteId : Long) : NotificationCompat.Builder? {
		return NotificationCompat.Builder(context, context.getString(R.string.note_geofence_notification_channel_name))
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(context.getString(R.string.note_geofence_notification_title))
				.setContentText(getBody(noteBody))
				.setContentIntent(getNoteGeofenceLocationPendingIntent(context, noteId))
				.setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
				.addAction(getOpenNoteGeofenceAction(context, noteId))
				.addAction(dismissNoteGeofenceNotificationAction(context, noteId))
	}

	private fun getDismissNoteGeofenceNotificationPendingIntent(context : Context, noteId : Long) : PendingIntent? {
		val dismissIntent = Intent(context, GeofenceReminderReceiver::class.java)
		dismissIntent.action = Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION
		dismissIntent.putExtra(Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION, noteId) // just treat it as key
		return PendingIntent.getBroadcast(context, noteId.toInt(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
	}

	private fun dismissNoteGeofenceNotificationAction(context : Context, noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_dismiss_notification_action,
				context.getString(R.string.note_notification_dismiss_action_label),
				getDismissNoteGeofenceNotificationPendingIntent(context, noteId))
	}

	private fun getOpenNoteGeofenceAction(context : Context, noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_notification_open,
				context.getString(R.string.note_notification_open_action_label),
				getNoteGeofenceLocationPendingIntent(context, noteId))
	}

	fun dismissNoteGeofenceReminderNotification(context : Context, noteId : Long) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(context.resources.getInteger(R.integer.note_geofence_reminder_notification_id) + noteId.toInt())
	}

	private fun getNoteGeofenceLocationPendingIntent(context : Context, noteId : Long) : PendingIntent? {
		val noteIntent = Intent(context, AddEditNoteActivity::class.java)
		noteIntent.putExtra(Constants.ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN, true)
		noteIntent.putExtra(Constants.NOTE_INTENT_KEY, noteId)
		return TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(noteIntent)
			getPendingIntent(noteId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
		}
	}

	fun sendNoteTimeReminderNotification(context : Context, noteBody : String, noteId : Long) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(context.getString(R.string.notification_id),
					context.getString(R.string.notification_note_time_reminder_channel_name),
					NotificationManager.IMPORTANCE_HIGH)
			notificationManager.createNotificationChannel(channel)
		}

		val builder = getNoteTimeReminderNotificationBuilder(context, noteBody, noteId)

		notificationManager.notify(context.resources.getInteger(R.integer.note_reminder_notification_id) + noteId.toInt(),
				builder?.build())
	}

	private fun getNoteTimeReminderNotificationBuilder(context : Context,
													   noteBody : String,
													   noteId : Long) : NotificationCompat.Builder? {
		return NotificationCompat.Builder(context, context.getString(R.string.notification_id))
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(context.getString(R.string.note_time_reminder_notification_title))
				.setContentText(getBody(noteBody))
				.setContentIntent(getOpenNoteNotificationPendingIntent(context, noteId))
				.setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
				.addAction(getOpenNoteAction(context, noteId)).addAction(dismissNotificationsAction(context, noteId))
	}


	private fun getOpenNoteAction(context : Context, noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_notification_open,
				context.getString(R.string.note_notification_open_action_label),
				getOpenNoteNotificationPendingIntent(context, noteId))
	}

	private fun dismissNotificationsAction(context : Context, noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_dismiss_notification_action,
				context.getString(R.string.note_notification_dismiss_action_label),
				getDismissNotificationPendingIntent(context, noteId))
	}

	private fun getOpenNoteNotificationPendingIntent(context : Context, noteId : Long) : PendingIntent? {
		val noteIntent = Intent(context, AddEditNoteActivity::class.java)
		noteIntent.putExtra(Constants.ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN, true)
		noteIntent.putExtra(Constants.NOTE_INTENT_KEY, noteId)
		return TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(noteIntent)
			getPendingIntent(noteId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
		}
	}

	private fun getDismissNotificationPendingIntent(context : Context, noteId : Long) : PendingIntent? {
		val dismissIntent = Intent(context, NoteReminderReceiver::class.java)
		dismissIntent.action = Constants.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION
		dismissIntent.putExtra(Constants.NOTE_INTENT_KEY, noteId)
		return PendingIntent.getBroadcast(context, noteId.toInt(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
	}

	fun dismissNoteReminderNotification(context : Context, id : Int) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(context.resources.getInteger(R.integer.note_reminder_notification_id) + id)
	}

	@Suppress("DEPRECATION")
	private fun getBody(body : String) : Spanned {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			HtmlCompat.fromHtml("$body ...", Html.FROM_HTML_MODE_LEGACY)
		} else {
			Html.fromHtml(body)
		}
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