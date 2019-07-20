package elamien.abdullah.socialnote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class NotesDatabase : RoomDatabase() {

    companion object {
        private var sInstance: NotesDatabase? = null
        fun getDatabase(context: Context): NotesDatabase? {
            if (sInstance == null) {
                synchronized(NotesDatabase::class) {
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java, "chapter.db"
                    )
                        .build()
                }
            }
            return sInstance
        }
    }

    abstract fun notesDao(): NoteDao

}