package elamien.abdullah.socialnote.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.database.notes.NoteDao
import elamien.abdullah.socialnote.receiver.GeofenceReminderReceiver
import elamien.abdullah.socialnote.utils.Constants
import org.koin.core.KoinComponent
import org.koin.core.inject


class GeofenceService : JobIntentService(), KoinComponent {
	private val mNotesDao by inject<NoteDao>()

	fun enqueueQueryingAndAddingGeofencesJob(applicationContext : Context?, intent : Intent) {
		enqueueWork(applicationContext!!,
				GeofenceService::class.java,
				Constants.GEOFENCE_RETRIEVER_INTENT_JOB_ID,
				intent)
	}

	override fun onHandleWork(intent : Intent) {
		val action = intent.action
		if (action == Constants.RE_ADD_GEOFNECES_INTENT_ACTION) {
			queryAndAddGeofences()
		}
	}

	private fun queryAndAddGeofences() {
		val geofencesList = mNotesDao.getAllGeofencesNotes()
		geofencesList.forEach { note ->
			createGeofences(note)
		}
	}

	private fun createGeofences(note : Note) {
		val latLong =
			LatLng(note.geofence?.noteGeofenceLatitude!!, note.geofence?.noteGeofenceLongitude!!)
		addGeofence(createGeofenceRequest(createAGeofencing(latLong, note.id!!)), note.id!!)
	}

	@SuppressLint("MissingPermission")
	private fun addGeofence(geofencingRequest : GeofencingRequest, id : Long) {
		val client = LocationServices.getGeofencingClient(applicationContext)
		client.addGeofences(geofencingRequest, createGeofencePendingIntent(id))
	}

	private fun createGeofencePendingIntent(id : Long) : PendingIntent {
		val intent = Intent(applicationContext, GeofenceReminderReceiver::class.java)
		intent.action = Constants.NOTE_GEOFENCE_REMINDER_ACTION
		intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY, id)
		return PendingIntent.getBroadcast(applicationContext,
				id.toInt(),
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT)

	}

	private fun createGeofenceRequest(geofence : Geofence) : GeofencingRequest {
		return GeofencingRequest.Builder()
				.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
				.addGeofence(geofence)
				.build()
	}

	private fun createAGeofencing(latLng : LatLng, id : Long) : Geofence {
		return Geofence.Builder()
				.setRequestId("geo_fence_reminder$id")
				.setCircularRegion(latLng.latitude,
						latLng.longitude,
						Constants.GEOFENCE_REMINDER_RADIUS)
				.setExpirationDuration(Constants.GEOFENCE_EXPIRE_DATE)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
				.build()
	}


	companion object {
		fun getGeofenceService() = GeofenceService()
	}
}