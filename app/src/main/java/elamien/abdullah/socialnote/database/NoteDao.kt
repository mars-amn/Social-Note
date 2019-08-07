package elamien.abdullah.socialnote.database

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
    fun insertNote(note : Note) : Single<Long>

    @Query("SELECT * FROM Notes ORDER BY date_created DESC")
    fun getNotes() : DataSource.Factory<Int, Note>

    @Query("SELECT * FROM Notes WHERE note_id =:id")
    fun getNote(id : Long?) : Flowable<Note>

    @Query("SELECT * FROM Notes WHERE note_body LIKE :query OR note_title LIKE :query ORDER BY date_created DESC")
    fun searchNotes(query : String) : DataSource.Factory<Int, Note>

    @Update
    fun updateNote(note : Note)

    @Delete
    fun deleteNote(note : Note) : Int

    /**
     * DANGER!
     * DO NOT call this function
     * calling it probably will make you regret
     */
    @Query("DELETE FROM Notes")
    fun nukeTable()
}