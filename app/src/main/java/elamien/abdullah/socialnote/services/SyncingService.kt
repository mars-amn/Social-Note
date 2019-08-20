package elamien.abdullah.socialnote.services

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.database.notes.NoteDao
import elamien.abdullah.socialnote.utils.Constants
import org.koin.core.KoinComponent
import org.koin.core.inject


class SyncingService : JobIntentService(), KoinComponent {

	private val mNotesDao by inject<NoteDao>()
	private val mFirestore by inject<FirebaseFirestore>()
	private val mFirebaseAuth by inject<FirebaseAuth>()
	fun enqueueSyncNewNoteService(context : Context, intent : Intent) {
		enqueueWork(context, SyncingService::class.java, Constants.SYNC_NEW_NOTE_INTENT_JOB_ID, intent)
	}

	override fun onHandleWork(intent : Intent) {
		val action = intent.action
		if (action == Constants.SYNC_NEW_NOTE_INTENT_ACTION) {
			val noteId = intent.getLongExtra(Constants.SYNC_NOTE_ID_INTENT_KEY, -1)
			addNoteToFirestore(noteId)
		}
	}

	private fun addNoteToFirestore(noteId : Long) {
		val note = mNotesDao.getNoteForSync(noteId)
		if (note != null) {

			val noteMap = getMappedNote(note)

			mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
					.document(mFirebaseAuth.currentUser?.uid!!)
					.collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
					.add(noteMap)
					.addOnFailureListener { }
		}

	}

	private fun getMappedNote(note : Note) : HashMap<String, Any> {
		val noteMap = HashMap<String, Any>()
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_TITLE] = note.noteTitle!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_BODY] = note.note!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_CREATED] = note.dateCreated!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_MODIFIED] = note.dateModified!!
		if (note.geofence != null) {
			noteMap[Constants.FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER] =
				GeoPoint(note.geofence?.noteGeofenceLatitude!!, note.geofence?.noteGeofenceLongitude!!)
		}
		if (note.timeReminder != null) {
			noteMap[Constants.FIRESTORE_SYNCED_NOTE_TIME_REMINDER] = note.timeReminder!!.timeReminder!!
		}
		return noteMap
	}

	companion object {
		fun getSyncingService() = SyncingService()
	}
}