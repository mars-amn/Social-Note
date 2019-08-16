package elamien.abdullah.socialnote.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import elamien.abdullah.socialnote.database.AppDatabase
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.utils.NotificationsUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class GeofenceReminderReceiver : BroadcastReceiver() {

	private var mDatabase : AppDatabase? = null
	private val locationPermissions =
		arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

	override fun onReceive(context : Context?, intent : Intent?) {
		if (context != null && intent != null) {
			val action = intent.action
			if (action == Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION) {
				dismissNotification(context)
			} else if (action == Constants.NOTE_GEOFENCE_REMINDER_ACTION) {
				sendNoteGeofenceNotification(context, intent)
				removeGeofenceRequest(intent, context)
			} else if (action == Intent.ACTION_BOOT_COMPLETED && EasyPermissions.hasPermissions(context,
						*locationPermissions)) {
				retrieveGeofencesAndAddThemAgain(context)
			}
		}
	}

	private fun sendNoteGeofenceNotification(context : Context, intent : Intent) {
		mDatabase = AppDatabase.getDatabase(context)
		val disposables = CompositeDisposable()
		disposables.add(Observable.fromCallable {
			mDatabase?.notesDao()
					?.getGeofenceNote(intent.getLongExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY, -1))
		}.subscribeOn(Schedulers.io()).subscribe { note ->
			if (note != null) {
				NotificationsUtils.getNotificationUtils()
						.sendNoteGeofenceReminderNotification(context, note.note!!, note.id!!)
			}
		})
	}

	private fun dismissNotification(context : Context) {
		NotificationsUtils.getNotificationUtils().dismissNoteGeofenceReminderNotification(context)
	}

	private fun removeGeofenceRequest(intent : Intent?, context : Context?) {
		val event = GeofencingEvent.fromIntent(intent)
		val triggeredGeofences = event.triggeringGeofences
		val triggeredGeofencesRequestIds = arrayListOf<String>()
		for (geofence in triggeredGeofences) {
			triggeredGeofencesRequestIds.add(geofence.requestId)
		}
		LocationServices.getGeofencingClient(context!!).removeGeofences(triggeredGeofencesRequestIds)
	}

	private fun retrieveGeofencesAndAddThemAgain(context : Context) {
		val disposables = CompositeDisposable()
		mDatabase = AppDatabase.getDatabase(context)
		disposables.add(Observable.fromCallable {
			mDatabase?.notesDao()?.getAllGeofencesNotes()
		}.subscribeOn(Schedulers.io()).subscribe {
			it?.forEach { geo ->
				if (geo.geofence != null) {
					createGeofences(geo, context)
				}
			}
		})
	}

	private fun createGeofences(note : Note, context : Context) {
		val latLong = LatLng(note.geofence?.noteGeofenceLatitude!!, note.geofence?.noteGeofenceLongitude!!)
		addGeofence(createGeofenceRequest(createAGeofencing(latLong)), context, note.id!!)
	}

	@SuppressLint("MissingPermission")
	private fun addGeofence(geofencingRequest : GeofencingRequest, context : Context, id : Long) {
		val client = LocationServices.getGeofencingClient(context)
		client.addGeofences(geofencingRequest, createGeofencePendingIntent(context, id))
	}

	private fun createGeofencePendingIntent(context : Context, id : Long) : PendingIntent {
		val intent = Intent(context, GeofenceReminderReceiver::class.java)
		intent.action = Constants.NOTE_GEOFENCE_REMINDER_ACTION
		intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY, id)
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

	}

	private fun createGeofenceRequest(geofence : Geofence) : GeofencingRequest {
		return GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
				.addGeofence(geofence).build()
	}

	private fun createAGeofencing(it : LatLng) : Geofence {
		return Geofence.Builder().setRequestId("geo_fence_reminder${Date().time}")
				.setCircularRegion(it.latitude, it.longitude, Constants.GEOFENCE_REMINDER_RADIUS)
				.setExpirationDuration(Constants.GEOFENCE_EXPIRE_DATE)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT).build()
	}
}