package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import elamien.abdullah.socialnote.models.Comment
import elamien.abdullah.socialnote.models.Post
import elamien.abdullah.socialnote.utils.Constants
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostRepository : IPostRepository, KoinComponent {


	private val mFirestore : FirebaseFirestore by inject()
	override fun createComment(documentName : String, comment : Comment) {
		mFirestore.collection(Constants.FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.update(Constants.FIRESTORE_POSTS_POST_COMMENTS, FieldValue.arrayUnion(comment))
				.addOnCompleteListener { }
				.addOnFailureListener { }
	}

	override fun createNewPost(post : Post) {
		val documentName = "${post.authorUId}${Date().time}"
		post.documentName = documentName
		mFirestore.collection(Constants.FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.set(getMappedPost(post), SetOptions.merge())
				.addOnCompleteListener { }
				.addOnFailureListener {}
	}

	private fun getMappedPost(post : Post) : HashMap<String, Any> {
		val postMap = HashMap<String, Any>()
		postMap[Constants.FIRESTORE_POSTS_POST_BODY] = post.post!!
		postMap[Constants.FIRESTORE_POSTS_POST_AUTHOR_NAME] = post.authorName!!
		postMap[Constants.FIRESTORE_POSTS_POST_AUTHOR_ID] = post.authorUId!!
		postMap[Constants.FIRESTORE_POSTS_POST_AUTHOR_IMAGE] = post.authorImage!!
		postMap[Constants.FIRESTORE_POSTS_POST_CATEGORY_NAME] = post.categoryName!!
		postMap[Constants.FIRESTORE_POSTS_POST_DATE_CREATED] = Date()
		postMap[Constants.FIRESTORE_POSTS_POST_DOC_NAME] = post.documentName!!
		return postMap
	}

	override fun getPostsFeed() : FirestoreRecyclerOptions<Post> {
		val query = mFirestore.collection(Constants.FIRESTORE_POSTS_COLLECTION_NAME)
				.orderBy(Constants.FIRESTORE_POSTS_POST_DATE_CREATED, Query.Direction.DESCENDING)
		return FirestoreRecyclerOptions.Builder<Post>()
				.setQuery(query, Post::class.java)
				.build()
	}

	override fun getCommentsFeed(documentName : String) : LiveData<List<Comment>> {
		val comments = MutableLiveData<List<Comment>>()
		mFirestore.collection(Constants.FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.addSnapshotListener { snapshot, e ->
					if (e != null) {

					} else {
						val post = snapshot?.toObject(Post::class.java)
						comments.value = post?.comments
					}
				}
		return comments
	}
}