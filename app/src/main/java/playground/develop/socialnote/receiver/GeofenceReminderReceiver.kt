package playground.develop.socialnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log.d
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import playground.develop.socialnote.database.local.AppDatabase
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.NotificationsUtils

class GeofenceReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val action = intent.action
            d("geofence", action!!)
            if (action == Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION) {
                dismissNotification(context,
                                    intent.getLongExtra(Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION,
                                                        -1))
                d("geofence", "Dismissing")
            } else if (action == Constants.NOTE_GEOFENCE_REMINDER_ACTION) {
                d("geofence", "sending notification")
                sendNoteGeofenceNotification(context, intent)
                removeGeofenceRequest(intent, context)
            }
        }
    }

    private fun sendNoteGeofenceNotification(context: Context, intent: Intent) {
        AsyncTask.execute {
            val note = AppDatabase.getDatabase(context)
                    ?.notesDao()
                    ?.getGeofenceNote(intent.getLongExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY,
                                                          -1))
            if (note != null) {
                d("geofence", "in GeofenceReminderReceiver sendNoteGeofenceNotification")
                NotificationsUtils.getNotificationUtils()
                        .sendNoteGeofenceReminderNotification(context, note.note!!, note.id!!)
            }
        }
    }

    private fun dismissNotification(context: Context, noteId: Long) {
        NotificationsUtils.getNotificationUtils()
                .dismissNoteGeofenceReminderNotification(context, noteId)
    }

    private fun removeGeofenceRequest(intent: Intent?, context: Context?) {
        val event = GeofencingEvent.fromIntent(intent)
        val triggeredGeofences = event.triggeringGeofences
        val triggeredGeofencesRequestIds = arrayListOf<String>()
        for (geofence in triggeredGeofences) {
            triggeredGeofencesRequestIds.add(geofence.requestId)
        }
        LocationServices.getGeofencingClient(context!!)
                .removeGeofences(triggeredGeofencesRequestIds)
    }
}