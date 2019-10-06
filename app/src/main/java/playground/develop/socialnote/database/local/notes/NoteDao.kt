package playground.develop.socialnote.database.local.notes

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note): Single<Long>

    @Query("SELECT * FROM Notes ORDER BY date_created DESC")
    fun getNotes(): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM Notes WHERE note_id =:id")
    fun getNote(id: Long?): Flowable<Note>

    @Query("SELECT * FROM Notes WHERE note_body LIKE :query OR note_title LIKE :query ORDER BY date_created DESC")
    fun searchNotes(query: String): DataSource.Factory<Int, Note>

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note): Int

    /**
     * Geofence related queries
     */
    @Query("SELECT * FROM Notes WHERE location_noteGeofenceLongitude IS NOT NULL AND location_noteGeofenceLatitude IS NOT NULL")
    fun getAllGeofencesNotes(): List<Note>

    @Query("SELECT * FROM Notes WHERE location_noteGeofenceLatitude IS NOT NULL AND location_noteGeofenceLongitude IS NOT NULL AND note_id =:id")
    fun getGeofenceNote(id: Long): Note

    /**
     * Time reminder related queries
     */
    @Query("SELECT * FROM Notes WHERE preferred_timeReminder IS NOT NULL")
    fun getTimeReminderNotes(): List<Note>

    /**
     * Syncing related queries
     */
    @Query("SELECT * FROM Notes WHERE note_id =:id")
    fun getNoteForSync(id: Long): Note?

    @Query("SELECT * FROM Notes WHERE is_synced = 0")
    fun getNotesForSyncing(): List<Note>

    @Query("SELECT * FROM Notes WHERE need_update = 1")
    fun getNotesNeededForUpdate(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSyncedNotes(notes: List<Note>)

    /**
     * DANGER!
     * DO NOT call this function
     * calling it probably will make you regret
     */
    @Query("DELETE FROM Notes")
    fun nukeTable()
}