package elamien.abdullah.socialnote.database.remote.firestore.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.firebase.Timestamp
import java.util.*

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
data class Comment(var commentAuthorToken : String? = null,
				   var authorRegisterToken : String? = null,
				   var documentId : String? = null,
				   var comment : String? = null,
				   var authorImage : String? = null,
				   var authorName : String? = null,
				   var authorUId : String? = null,
				   private var dateCreated : Timestamp? = null) {

	fun getDateCreated() : Date {
		return dateCreated!!.toDate()
	}

	fun setDateCreated(date : Timestamp) {
		this.dateCreated = date
	}

	fun isCommentEmpty() : Boolean {
		return comment == null || authorImage == null || authorName == null || authorUId == null
	}

	companion object {
		@BindingAdapter("authorCommentImageUrl")
		@JvmStatic
		fun loadAuthorCommentImage(view : ImageView, imageUrl : String) {
			view.load(imageUrl) {
				crossfade(true)
				transformations(CircleCropTransformation())
			}

		}
	}
}
