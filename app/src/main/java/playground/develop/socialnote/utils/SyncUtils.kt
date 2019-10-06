package playground.develop.socialnote.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import playground.develop.socialnote.services.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Created by AbdullahAtta on 04-Oct-19.
 */
class SyncUtils : KoinComponent {
    private val mDisposables = CompositeDisposable()
    private val mNotesDao by inject<NoteDao>()
    private val mFirestore by inject<FirebaseFirestore>()
    private val mFirebaseAuth by inject<FirebaseAuth>()

    fun startSyncWorker(context: Context) {
        val constraintsBuilder = Constraints.Builder()
            .setRequiresCharging(true)

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraintsBuilder.build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                Constants.SYNC_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
    }

    fun loadNotesFromFirestore() {
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
            .get()
            .addOnCompleteListener { query ->
                val syncedNotes = ArrayList<SyncedNote>()
                val localList = ArrayList<Note>()
                if (query.exception == null && query.isSuccessful) {
                    query.result?.forEach { document ->
                        syncedNotes.add(document.toObject(SyncedNote::class.java))
                    }

                    if (syncedNotes.isNotEmpty()) {
                        syncedNotes.forEach { syncedNote ->
                            val noteReminder = syncedNote.timeReminder
                            val geoLocation = syncedNote.geofenceLocation

                            val note = Note(
                                syncedNote.noteTitle,
                                syncedNote.noteBody,
                                syncedNote.getDateCreated(),
                                syncedNote.getDateModified()
                            )
                            note.id = syncedNote.noteId
                            note.isSynced = syncedNote.isSynced
                            if (noteReminder != null) {
                                note.timeReminder = NoteReminder(syncedNote.timeReminder)

                            }
                            if (geoLocation != null) {
                                note.geofence = NoteGeofence(
                                    syncedNote.geofenceLocation?.latitude,
                                    syncedNote.geofenceLocation?.longitude
                                )
                            }
                            localList.add(note)
                        }
                        populateLocalDatabase(localList)
                    }
                }
            }
            .addOnFailureListener {
            }
    }

    private fun populateLocalDatabase(list: ArrayList<Note>) {
        mDisposables.add(
            Observable.fromCallable { mNotesDao.insertSyncedNotes(list) }.subscribeOn(
                Schedulers.io()
            ).subscribe()
        )
    }

    fun addNotesToFirestore() {
        mDisposables.add(
            Observable.fromCallable { mNotesDao.getNotesForSyncing() }.subscribeOn(
                Schedulers.io()
            ).subscribe { notes ->
                notes.forEach { note ->
                    addNoteToFirestore(note)
                }
            }
        )
    }

    fun syncNeedNoteUpdatesToFirestore() {
        mDisposables.add(
            Observable.fromCallable { mNotesDao.getNotesNeededForUpdate() }.subscribeOn(
                Schedulers.io()
            ).subscribe { notes ->
                notes.forEach { note ->
                    updateNoteInFirestore(note)
                }
            }
        )
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
                mDisposables.add(
                    Observable.fromCallable { mNotesDao.updateNote(note) }.subscribeOn(
                        Schedulers.io()
                    ).subscribe()
                )
            }
            .addOnFailureListener {}
    }

    private fun addNoteToFirestore(note: Note) {
        mFirestore.collection(Constants.FIRESTORE_SYNCED_NOTES_COLLECTION_NAME)
            .document(mFirebaseAuth.currentUser?.uid!!)
            .collection(Constants.FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME)
            .document(getDocumentName(note.id!!))
            .set(getMappedNote(note))
            .addOnSuccessListener {
                note.isSynced = true
                mDisposables.add(
                    Observable.fromCallable { mNotesDao.updateNote(note) }.subscribeOn(
                        Schedulers.io()
                    ).subscribe()
                )
            }
            .addOnFailureListener { }
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
            noteMap[Constants.FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER] = GeoPoint(
                note.geofence?.noteGeofenceLatitude!!,
                note.geofence?.noteGeofenceLongitude!!
            )
        }
        if (note.timeReminder != null) {
            noteMap[Constants.FIRESTORE_SYNCED_NOTE_TIME_REMINDER] =
                note.timeReminder!!.timeReminder!!
        }
        return noteMap
    }

    private fun getDocumentName(id: Long): String {
        val user = mFirebaseAuth.currentUser!!
        val uid = user.uid
        val noteId = id.toString()
        return uid + "note" + noteId
    }

    companion object {
        fun getSyncUtils(): SyncUtils {
            return SyncUtils()
        }
    }
}