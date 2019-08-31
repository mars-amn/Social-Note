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
import elamien.abdullah.socialnote.receiver.NotificationReceiver
import elamien.abdullah.socialnote.ui.AddEditNoteActivity
import elamien.abdullah.socialnote.ui.CommentActivity
import elamien.abdullah.socialnote.utils.Constants.Companion.ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN
import elamien.abdullah.socialnote.utils.Constants.Companion.ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN
import elamien.abdullah.socialnote.utils.Constants.Companion.DISMISS_NOTE_GEOFENCE_NOTIFICATION
import elamien.abdullah.socialnote.utils.Constants.Companion.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION
import elamien.abdullah.socialnote.utils.Constants.Companion.DISMISS_POST_COMMENT_NOTIFICATION_ACTION
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POST_DOC_INTENT_KEY
import elamien.abdullah.socialnote.utils.Constants.Companion.NOTE_INTENT_KEY
import elamien.abdullah.socialnote.utils.Constants.Companion.OPEN_FROM_NOTIFICATION_COMMENT
import java.util.*


/**
 * Created by AbdullahAtta on 8/9/2019.
 */
class NotificationsUtils {

	/**
	 * Notification upon user like & text
	 */
	fun sendPostInteractNotification(context : Context,
									 text : String,
									 title : String,
									 documentId : String,
									 token : String) {
		val notificationId = Date().time

		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel =
				NotificationChannel(context.getString(R.string.notification_post_channel_id),
						context.getString(R.string.notification_social_posts_channel_name),
						NotificationManager.IMPORTANCE_HIGH)
			notificationManager.createNotificationChannel(channel)
		}
		val builder = getPostInteractNotificationBuilder(context,
				notificationId.toInt(),
				text,
				title,
				documentId,
				token)
		notificationManager.notify(notificationId.toInt(), builder.build())
	}

	private fun getPostInteractNotificationBuilder(context : Context,
												   notificationId : Int,
												   text : String,
												   title : String,
												   documentId : String,
												   token : String) : NotificationCompat.Builder {

		return NotificationCompat.Builder(context,
				context.getString(R.string.notification_post_channel_id))
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(title)
				.setContentText(text)
				.setContentIntent(getPostContentPendingIntent(context,
						notificationId,
						documentId,
						token))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.addAction(getDismissPostNotificationAction(context, notificationId))
				.addAction(getOpenPostNotificationAction(context,
						notificationId,
						documentId,
						token))

	}

	private fun getOpenPostNotificationAction(context : Context,
											  notificationId : Int,
											  documentId : String,
											  token : String) : NotificationCompat.Action? {
		return NotificationCompat.Action(R.drawable.ic_notification_open,
				context.getString(R.string.open_post_notification_action_label),
				getOpenPostPendingIntent(context, notificationId, documentId, token))
	}

	private fun getOpenPostPendingIntent(context : Context,
										 notificationId : Int,
										 documentId : String,
										 token : String) : PendingIntent? {
		val openIntent = Intent(context, CommentActivity::class.java)
		openIntent.putExtra(OPEN_FROM_NOTIFICATION_COMMENT, true)
		openIntent.putExtra(FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY, token)
		openIntent.putExtra(FIRESTORE_POST_DOC_INTENT_KEY, documentId)
		openIntent.putExtra(DISMISS_POST_COMMENT_NOTIFICATION_ACTION, notificationId)
		return TaskStackBuilder.create(context)
				.run {
					addNextIntentWithParentStack(openIntent)
					getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT)
				}
	}

	private fun getDismissPostNotificationAction(context : Context,
												 notificationId : Int) : NotificationCompat.Action? {
		return NotificationCompat.Action(R.drawable.ic_dismiss_notification_action,
				context.getString(R.string.note_notification_dismiss_action_label),
				getDismissPostNotificationIntent(context, notificationId))
	}

	private fun getDismissPostNotificationIntent(context : Context,
												 notificationId : Int) : PendingIntent? {
		val dismissIntent = Intent(context, NotificationReceiver::class.java)
		dismissIntent.action = DISMISS_POST_COMMENT_NOTIFICATION_ACTION
		dismissIntent.putExtra(DISMISS_POST_COMMENT_NOTIFICATION_ACTION, notificationId)
		return PendingIntent.getBroadcast(context,
				notificationId,
				dismissIntent,
				PendingIntent.FLAG_UPDATE_CURRENT)
	}

	private fun getPostContentPendingIntent(context : Context,
											notificationId : Int,
											documentId : String,
											token : String) : PendingIntent? {

		val commentIntent = Intent(context, CommentActivity::class.java)
		commentIntent.putExtra(FIRESTORE_POST_DOC_INTENT_KEY, documentId)
		commentIntent.putExtra(FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY, token)
		commentIntent.putExtra(OPEN_FROM_NOTIFICATION_COMMENT, true)
		return TaskStackBuilder.create(context)
				.run {
					addNextIntentWithParentStack(commentIntent)
					getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT)
				}
	}

	fun sendNoteGeofenceReminderNotification(context : Context, noteBody : String, noteId : Long) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel =
				NotificationChannel(context.getString(R.string.note_geofence_notification_id),
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
		return NotificationCompat.Builder(context,
				context.getString(R.string.note_geofence_notification_channel_name))
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(context.getString(R.string.note_geofence_notification_title))
				.setContentText(getBody(noteBody))
				.setContentIntent(getNoteGeofenceLocationPendingIntent(context, noteId))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.addAction(getOpenNoteGeofenceAction(context, noteId))
				.addAction(dismissNoteGeofenceNotificationAction(context, noteId))
	}

	private fun getDismissNoteGeofenceNotificationPendingIntent(context : Context,
																noteId : Long) : PendingIntent? {
		val dismissIntent = Intent(context, GeofenceReminderReceiver::class.java)
		dismissIntent.action = DISMISS_NOTE_GEOFENCE_NOTIFICATION
		dismissIntent.putExtra(DISMISS_NOTE_GEOFENCE_NOTIFICATION, noteId) // just treat it as key
		return PendingIntent.getBroadcast(context,
				noteId.toInt(),
				dismissIntent,
				PendingIntent.FLAG_UPDATE_CURRENT)
	}

	private fun dismissNoteGeofenceNotificationAction(context : Context,
													  noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_dismiss_notification_action,
				context.getString(R.string.note_notification_dismiss_action_label),
				getDismissNoteGeofenceNotificationPendingIntent(context, noteId))
	}

	private fun getOpenNoteGeofenceAction(context : Context,
										  noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_notification_open,
				context.getString(R.string.note_notification_open_action_label),
				getNoteGeofenceLocationPendingIntent(context, noteId))
	}

	fun dismissNoteGeofenceReminderNotification(context : Context, noteId : Long) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(context.resources.getInteger(R.integer.note_geofence_reminder_notification_id) + noteId.toInt())
	}

	private fun getNoteGeofenceLocationPendingIntent(context : Context,
													 noteId : Long) : PendingIntent? {
		val noteIntent = Intent(context, AddEditNoteActivity::class.java)
		noteIntent.putExtra(ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN, true)
		noteIntent.putExtra(NOTE_INTENT_KEY, noteId)
		return TaskStackBuilder.create(context)
				.run {
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
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.addAction(getOpenNoteAction(context, noteId))
				.addAction(dismissNotificationsAction(context, noteId))
	}


	private fun getOpenNoteAction(context : Context, noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_notification_open,
				context.getString(R.string.note_notification_open_action_label),
				getOpenNoteNotificationPendingIntent(context, noteId))
	}

	private fun dismissNotificationsAction(context : Context,
										   noteId : Long) : NotificationCompat.Action {
		return NotificationCompat.Action(R.drawable.ic_dismiss_notification_action,
				context.getString(R.string.note_notification_dismiss_action_label),
				getDismissNotificationPendingIntent(context, noteId))
	}

	private fun getOpenNoteNotificationPendingIntent(context : Context,
													 noteId : Long) : PendingIntent? {
		val noteIntent = Intent(context, AddEditNoteActivity::class.java)
		noteIntent.putExtra(ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN, true)
		noteIntent.putExtra(NOTE_INTENT_KEY, noteId)
		return TaskStackBuilder.create(context)
				.run {
					addNextIntentWithParentStack(noteIntent)
					getPendingIntent(noteId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
				}
	}

	private fun getDismissNotificationPendingIntent(context : Context,
													noteId : Long) : PendingIntent? {
		val dismissIntent = Intent(context, NoteReminderReceiver::class.java)
		dismissIntent.action = DISMISS_NOTE_TIME_REMINDER_NOTIFICATION
		dismissIntent.putExtra(NOTE_INTENT_KEY, noteId)
		return PendingIntent.getBroadcast(context,
				noteId.toInt(),
				dismissIntent,
				PendingIntent.FLAG_UPDATE_CURRENT)
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

	fun dismissCommentNotification(context : Context, notificationId : Int) {
		val notificationManager : NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(notificationId)
	}

	companion object {
		fun getNotificationUtils() : NotificationsUtils {
			return NotificationsUtils()
		}
	}
}