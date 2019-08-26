package elamien.abdullah.socialnote.database.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*

/**
 * Created by AbdullahAtta on 25-Aug-19.
 *
 * Used to represent a single synced note on Firestore
 */
class SyncedNote {

	var noteTitle : String? = null
	var noteBody : String? = null
	private var dateCreated : Timestamp? = null
	private var dateModified : Timestamp? = null
	var geofenceLocation : GeoPoint? = null
	var timeReminder : Long? = null
	var isSynced : Boolean = false
	var noteId : Long? = null
	fun getDateCreated() : Date {
		return dateCreated!!.toDate()
	}

	fun setDateCreated(date : Timestamp) {
		this.dateCreated = date
	}

	fun getDateModified() : Date {
		return dateModified!!.toDate()
	}

	fun setDateModified(date : Timestamp) {
		this.dateModified = date
	}
}