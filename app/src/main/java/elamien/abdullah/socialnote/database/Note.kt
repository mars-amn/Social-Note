package elamien.abdullah.socialnote.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by AbdullahAtta on 7/19/2019.
 */
@Entity(tableName = "Notes")
class Note(
    @ColumnInfo(name = "note_title") var noteTitle : String?, @ColumnInfo(name = "note_body") var note : String?, @ColumnInfo(
        name = "date_created"
    ) var dateCreated : Date?, @ColumnInfo(name = "date_modified") var dateModified : Date?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    var id : Long? = null

    override fun equals(other : Any?) : Boolean {
        if (other == null || other !is Note)
            return false
        return note == other.note && id == other.id
    }

    override fun hashCode() : Int {
        var result = noteTitle?.hashCode() ?: 0
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (dateCreated?.hashCode() ?: 0)
        result = 31 * result + (dateModified?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

}