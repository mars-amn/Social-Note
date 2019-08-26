package elamien.abdullah.socialnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import elamien.abdullah.socialnote.models.Comment
import elamien.abdullah.socialnote.models.Post
import elamien.abdullah.socialnote.repository.PostRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostViewModel : ViewModel(), KoinComponent {

	private val mPostRepository : PostRepository by inject()

	fun createPost(post : Post) {
		mPostRepository.createNewPost(post)
	}

	fun getPosts() : FirestoreRecyclerOptions<Post> {
		return mPostRepository.getPostsFeed()
	}

	fun createComment(postDocName : String, comment : Comment) {
		mPostRepository.createComment(postDocName, comment)
	}

	fun getComments(documentName : String) : LiveData<List<Comment>> {
		return mPostRepository.getCommentsFeed(documentName)
	}
}