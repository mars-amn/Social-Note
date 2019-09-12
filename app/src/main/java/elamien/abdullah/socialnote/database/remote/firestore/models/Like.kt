package elamien.abdullah.socialnote.database.remote.firestore.models

import android.os.Parcelable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.transform.CircleCropTransformation
import kotlinx.android.parcel.Parcelize

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
@Parcelize data class Like(var userLikerUId: String? = null,
                           var authorRegisterToken: String? = null,
                           var userRegisterToken: String? = null,
                           var userName: String? = null,
                           var documentId: String? = null,
                           var userImage: String? = null,
                           var userTitle: String? = null) : Parcelable {

    companion object {
        @BindingAdapter("likeUserImage")
        @JvmStatic
        fun loadUserLikeImage(view: ImageView, imageUrl: String) {
            view.load(imageUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }

        }
    }
}