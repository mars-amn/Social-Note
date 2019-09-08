package elamien.abdullah.socialnote.database.local.notes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import elamien.abdullah.socialnote.database.local.geofence.NoteGeofence
import elamien.abdullah.socialnote.database.local.reminder.NoteReminder
import java.util.*

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
@Entity(tableName = "Notes")
class Note(
    @ColumnInfo(name = "note_title") var noteTitle: String?, @ColumnInfo(name = "note_body") var note: String?, @ColumnInfo(
        name = "date_created"
    ) var dateCreated: Date?, @ColumnInfo(name = "date_modified") var dateModified: Date?, @Embedded(
        prefix = "location_"
    ) var geofence: NoteGeofence? = null, @Embedded(prefix = "preferred_") var timeReminder: NoteReminder? = null
) {

    @PrimaryKey
    @ColumnInfo(name = "note_id")
    var id: Long? = null

    @ColumnInfo(name = "is_synced")
    var isSynced: Boolean = false

    @ColumnInfo(name = "need_update")
    var isNeedUpdate: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Note) return false
        return note == other.note && id == other.id
    }

    override fun hashCode(): Int {
        var result = noteTitle?.hashCode() ?: 0
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (dateCreated?.hashCode() ?: 0)
        result = 31 * result + (dateModified?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

}