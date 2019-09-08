package elamien.abdullah.socialnote.database.local.notes

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}