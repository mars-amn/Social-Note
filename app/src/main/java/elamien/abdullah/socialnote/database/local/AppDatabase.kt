package elamien.abdullah.socialnote.database.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import elamien.abdullah.socialnote.database.local.notes.DateConverter
import elamien.abdullah.socialnote.database.local.notes.Note
import elamien.abdullah.socialnote.database.local.notes.NoteDao

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

	companion object {
		private var sInstance : AppDatabase? = null
		fun getDatabase(context : Context) : AppDatabase? {
			if (sInstance == null) {
				synchronized(AppDatabase::class) {
					sInstance = Room.databaseBuilder(context.applicationContext,
							AppDatabase::class.java,
							"notes.db")
							.build()
				}
			}
			return sInstance
		}
	}

	abstract fun notesDao() : NoteDao
}