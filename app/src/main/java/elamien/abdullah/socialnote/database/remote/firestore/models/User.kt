package elamien.abdullah.socialnote.database.remote.firestore.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by AbdullahAtta on 07-Sep-19.
 */
@Parcelize
data class User(var userName : String? = null,
				var userUid : String? = null,
				var userImage : String? = null,
				var userPostsCount : Int? = null,
				var userTitle : String? = null,
				private var dateSignup : Timestamp? = null) : Parcelable {

	fun getDateSignup() : Date {
		return dateSignup!!.toDate()
	}

	fun setDateSignup(date : Timestamp) {
		this.dateSignup = date
	}
}
