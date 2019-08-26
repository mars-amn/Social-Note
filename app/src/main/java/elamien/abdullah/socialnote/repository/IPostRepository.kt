package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import elamien.abdullah.socialnote.models.Comment
import elamien.abdullah.socialnote.models.Post

/**
 * Created by AbdullahAtta on 25-Aug-19.
 */
interface IPostRepository {

	fun createNewPost(post : Post)
	fun getPostsFeed() : FirestoreRecyclerOptions<Post>
	fun createComment(documentName : String, comment : Comment)
	fun getCommentsFeed(documentName : String) : LiveData<List<Comment>>
}