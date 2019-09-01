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
data class Post(var registerToken : String? = null,
				var post : String? = null,
				var authorName : String? = null,
				var categoryName : String? = null,
				var authorUId : String? = null,
				var authorImage : String? = null,
				private var dateCreated : Timestamp? = null) {

	var likes : ArrayList<Like>? = null
	var comments : List<Comment>? = null
	//	private var dateCreated : Timestamp? = null
	var documentName : String? = null

	fun getDateCreated() : Date {
		return dateCreated!!.toDate()
	}

	fun setDateCreated(date : Timestamp) {
		this.dateCreated = date
	}

	companion object {
		@BindingAdapter("authorPostImageUrl")
		@JvmStatic
		fun loadAuthorPostImage(view : ImageView, imageUrl : String) {
			view.load(imageUrl) {
				crossfade(true)
				transformations(CircleCropTransformation())
			}

		}
	}
}