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
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject


class SyncingService : JobIntentService(), KoinComponent {
	private val mNotesDao by inject<NoteDao>()
	private val mFirestore by inject<FirebaseFirestore>()
	private val mFirebaseAuth by inject<FirebaseAuth>()
	private val mDisposables = CompositeDisposable()
	fun enqueueSyncNewNoteService(context : Context, intent : Intent) {
		enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
	}

	fun enqueueSyncAllNotes(context : Context, intent : Intent) {
		enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
	}

	fun enqueueSyncNeededUpdateNotes(context : Context, intent : Intent) {
		enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
	}

	override fun onHandleWork(intent : Intent) {
		when (intent.action) {
			Constants.SYNC_NEW_NOTE_INTENT_ACTION -> {
				val noteId = intent.getLongExtra(Constants.SYNC_NOTE_ID_INTENT_KEY, -1)
				updateSyncNote(noteId)
			}
			Constants.SYNC_ALL_NOTES_INTENT_ACTION -> syncAllNotes()
			Constants.SYNC_NEEDED_UPDATES_NOTES_INTENT_ACTION -> syncNotesUpdates()
		}
	}

	private fun updateSyncNote(noteId : Long) {
		mDisposables.add(Observable.fromCallable { mNotesDao.getNoteForSync(noteId) }.subscribeOn(
				Schedulers.io()).subscribe { note ->
			updateNoteInFirestore(note!!)
		})
	}

	private fun updateNoteInFirestore(note : Note) {
		mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
				.document(mFirebaseAuth.currentUser?.uid!!)
				.collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
				.document("${mFirebaseAuth.currentUser?.uid!!}Note${note.id!!}")
				.update(getMappedNote(note))
				.addOnSuccessListener {
					note.isSynced = true
					note.isNeedUpdate = false
					mDisposables.add(Observable.fromCallable { mNotesDao.updateNote(note) }.subscribeOn(
							Schedulers.io()).subscribe())
				}
				.addOnFailureListener {}
	}

	private fun syncAllNotes() {
		mDisposables.add(Observable.fromCallable { mNotesDao.getNotesForSyncing() }.subscribeOn(
				Schedulers.io()).subscribe { notes ->
			notes.forEach { note ->
				addNoteToFirestore(note)
			}
		})
	}

	private fun addNoteToFirestore(note : Note) {
		mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
				.document(mFirebaseAuth.currentUser?.uid!!)
				.collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
				.document("${mFirebaseAuth.currentUser?.uid!!}Note${note.id}")
				.set(getMappedNote(note))
				.addOnSuccessListener {
					note.isSynced = true
					mDisposables.add(Observable.fromCallable { mNotesDao.updateNote(note) }.subscribeOn(
							Schedulers.io()).subscribe())
				}
				.addOnFailureListener { }
	}

	private fun syncNotesUpdates() {
		mDisposables.add(Observable.fromCallable { mNotesDao.getNotesNeededForUpdate() }.subscribeOn(
				Schedulers.io()).subscribe { notes ->
			notes.forEach { note ->
				updateNoteInFirestore(note)
			}
		})
	}


	private fun getMappedNote(note : Note) : HashMap<String, Any> {
		val noteMap = HashMap<String, Any>()
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_TITLE] = note.noteTitle!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_BODY] = note.note!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_CREATED] = note.dateCreated!!
		noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_MODIFIED] = note.dateModified!!
		if (note.geofence != null) {
			noteMap[Constants.FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER] =
				GeoPoint(note.geofence?.noteGeofenceLatitude!!,
						note.geofence?.noteGeofenceLongitude!!)
		}
		if (note.timeReminder != null) {
			noteMap[Constants.FIRESTORE_SYNCED_NOTE_TIME_REMINDER] =
				note.timeReminder!!.timeReminder!!
		}
		return noteMap
	}

	companion object {
		fun getSyncingService() = SyncingService()
	}
}