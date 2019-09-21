package playground.develop.socialnote.services

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import playground.develop.socialnote.database.local.geofence.NoteGeofence
import playground.develop.socialnote.database.local.notes.Note
import playground.develop.socialnote.database.local.notes.NoteDao
import playground.develop.socialnote.database.local.reminder.NoteReminder
import playground.develop.socialnote.database.remote.sync.SyncedNote
import playground.develop.socialnote.utils.Constants


class SyncingService : JobIntentService(), KoinComponent {

    private val mNotesDao by inject<NoteDao>()
    private val mFirestore by inject<FirebaseFirestore>()
    private val mFirebaseAuth by inject<FirebaseAuth>()
    private val mDisposables = CompositeDisposable()

    fun enqueueSyncNewNoteService(context: Context, intent: Intent) {
        enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
    }

    fun enqueueSyncAllNotes(context: Context, intent: Intent) {
        enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
    }

    fun enqueueSyncNeededUpdateNotes(context: Context, intent: Intent) {
        enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
    }

    fun enqueueSyncDeleteNote(context: Context, intent: Intent) {
        enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
    }

    fun enqueueCallSyncedNotes(context: Context, intent: Intent) {
        enqueueWork(context, SyncingService::class.java, Constants.SYNC_NOTE_SERVICE_JOB_ID, intent)
    }

    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            Constants.SYNC_UPDATE_NOTE_INTENT_ACTION -> {
                val noteId = intent.getLongExtra(Constants.SYNC_NOTE_ID_INTENT_KEY, -1)
                updateSyncNote(noteId)
            }
            Constants.SYNC_NEW_NOTE_INTENT_ACTION -> {
                val noteId = intent.getLongExtra(Constants.SYNC_NOTE_ID_INTENT_KEY, -1)
                syncNewNote(noteId)
            }
            Constants.SYNC_ALL_NOTES_INTENT_ACTION -> syncAllNotes()
            Constants.SYNC_NEEDED_UPDATES_NOTES_INTENT_ACTION -> syncNotesUpdates()
            Constants.SYNC_DELETE_NOTE_INTENT_ACTION -> deleteSyncNote(intent.getLongExtra(Constants.SYNC_NOTE_ID_INTENT_KEY,
                                                                                           -1))
            Constants.SYNC_CALL_NOTES_POPULATE_ROOM_INTENT_ACTION -> getAllNotesFromFirestore()

        }
    }

    private fun syncNewNote(noteId: Long) {
        mDisposables.add(Observable.fromCallable { mNotesDao.getNoteForSync(noteId) }.subscribeOn(
            Schedulers.io()).subscribe { note ->
            addNoteToFirestore(note!!)
        })
    }

    private fun updateSyncNote(noteId: Long) {
        mDisposables.add(Observable.fromCallable { mNotesDao.getNoteForSync(noteId) }.subscribeOn(
            Schedulers.io()).subscribe { note ->
            updateNoteInFirestore(note!!)
        })
    }

    private fun updateNoteInFirestore(note: Note) {
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
                .document(mFirebaseAuth.currentUser?.uid!!)
                .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
                .document(getDocumentName(note.id!!))
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

    private fun addNoteToFirestore(note: Note) {
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
                .document(mFirebaseAuth.currentUser?.uid!!)
                .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
                .document(getDocumentName(note.id!!))
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

    private fun deleteSyncNote(id: Long) {
        val documentName = getDocumentName(id)
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
                .document(mFirebaseAuth.currentUser?.uid!!)
                .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
                .document(documentName)
                .delete()
                .addOnSuccessListener { }
                .addOnFailureListener { }
    }

    private fun getDocumentName(id: Long): String {
        val user = mFirebaseAuth.currentUser!!
        val uid = user.uid
        val noteId = id.toString()
        return uid + "note" + noteId
    }

    private fun getAllNotesFromFirestore() {
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
                .document(mFirebaseAuth.currentUser?.uid!!)
                .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener { query ->
                    val syncedNotes = ArrayList<SyncedNote>()
                    val localList = ArrayList<Note>()
                    if (query.isSuccessful) {
                        query.result?.forEach { document ->
                            syncedNotes.add(document.toObject(SyncedNote::class.java))
                        }

                        if (syncedNotes.isNotEmpty()) {
                            syncedNotes.forEach { syncedNote ->
                                val noteReminder = syncedNote.timeReminder
                                val geoLocation = syncedNote.geofenceLocation

                                val note = Note(syncedNote.noteTitle,
                                                syncedNote.noteBody,
                                                syncedNote.getDateCreated(),
                                                syncedNote.getDateModified())
                                note.id = syncedNote.noteId
                                note.isSynced = syncedNote.isSynced
                                if (noteReminder != null) {
                                    note.timeReminder = NoteReminder(syncedNote.timeReminder)

                                }
                                if (geoLocation != null) {
                                    note.geofence = NoteGeofence(syncedNote.geofenceLocation?.latitude,
                                                                 syncedNote.geofenceLocation?.longitude)
                                }
                                localList.add(note)
                            }
                            populateLocalDatabase(localList)
                        }
                    }
                }
                .addOnFailureListener { }
    }

    private fun populateLocalDatabase(notesList: List<Note>) {
        mDisposables.add(Observable.fromCallable { mNotesDao.insertSyncedNotes(notesList) }.subscribeOn(
            Schedulers.io()).subscribe())
    }

    private fun getMappedNote(note: Note): HashMap<String, Any> {
        val noteMap = HashMap<String, Any>()
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_ID] = note.id!!
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_TITLE] = note.noteTitle!!
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_BODY] = note.note!!
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_CREATED] = note.dateCreated!!
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_DATE_MODIFIED] = note.dateModified!!
        noteMap[Constants.FIRESTORE_SYNCED_NOTE_IS_SYNCED] = true
        if (note.geofence != null) {
            noteMap[Constants.FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER] = GeoPoint(note.geofence?.noteGeofenceLatitude!!,
                                                                                  note.geofence?.noteGeofenceLongitude!!)
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