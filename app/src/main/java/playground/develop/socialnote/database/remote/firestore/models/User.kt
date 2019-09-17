package playground.develop.socialnote.database.remote.firestore.models

import android.os.Parcelable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by AbdullahAtta on 07-Sep-19.
 */
@Parcelize data class User(var userName: String? = null,
                           var userUid: String? = null,
                           var userImage: String? = null,
                           var userPostsCount: Int? = null,
                           var userTitle: String? = null,
                           private var dateSignup: Timestamp? = null,
                           var coverImage: String? = null) : Parcelable {

    fun getDateSignup(): Date {
        return dateSignup!!.toDate()
    }

    fun setDateSignup(date: Timestamp) {
        this.dateSignup = date
    }

    companion object {
        @BindingAdapter("userProfileImageUrl")
        @JvmStatic
        fun loadUserProfileImage(view: ImageView, imageUrl: String) {
            view.load(imageUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }

        }

        @BindingAdapter("userProfileCoverImageUrl")
        @JvmStatic
        fun loadUserProfileCoverImage(view: ImageView, imageUrl: String) {
            view.load(imageUrl) {
                crossfade(true)
            }

        }
    }
}
