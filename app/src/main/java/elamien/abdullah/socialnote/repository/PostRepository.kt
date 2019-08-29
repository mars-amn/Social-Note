package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import elamien.abdullah.socialnote.database.remote.firestore.models.Comment
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_IMAGE
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_REGISTER_TOKEN
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_UID
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COLLECTION_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COMMENT
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COMMENTER_AUTHOR_TOKEN
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_DATE_CREATED
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_POST_COMMENT_DOC_ID
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_COLLECTION_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_ID
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_IMAGE
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_BODY
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_CATEGORY_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_COMMENTS
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_DATE_CREATED
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_DOC_NAME
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_LIKES
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_REGISTER_TOKEN
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostRepository : IPostRepository, KoinComponent {


	private val mFirestore : FirebaseFirestore by inject()

	override fun createLikeOnPost(like : Like) {
		mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
				.document(like.documentId!!)
				.update(FIRESTORE_POSTS_POST_LIKES, FieldValue.arrayUnion(like))
				.addOnCompleteListener { }
				.addOnFailureListener { }
	}

	override fun createComment(documentName : String, comment : Comment) {
		mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.update(FIRESTORE_POSTS_POST_COMMENTS, FieldValue.arrayUnion(comment))
				.addOnCompleteListener { }
				.addOnFailureListener { }

		mFirestore.collection(FIRESTORE_COMMENTS_NOTIFICATION_COLLECTION_NAME)
				.document()
				.set(getMappedComment(comment))
				.addOnCompleteListener { }
				.addOnFailureListener { }
	}

	private fun getMappedComment(comment : Comment) : HashMap<String, Any> {
		val commentMap = HashMap<String, Any>()
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_REGISTER_TOKEN] =
			comment.authorRegisterToken!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_COMMENT] = comment.comment!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_NAME] = comment.authorName!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_IMAGE] = comment.authorImage!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_UID] = comment.authorUId!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_DATE_CREATED] = comment.getDateCreated()
		commentMap[FIRESTORE_COMMENTS_POST_COMMENT_DOC_ID] = comment.documentId!!
		commentMap[FIRESTORE_COMMENTS_NOTIFICATION_COMMENTER_AUTHOR_TOKEN] =
			comment.commentAuthorToken!!
		return commentMap
	}

	override fun createNewPost(post : Post) {
		val documentName = "${post.authorUId}${Date().time}"
		post.documentName = documentName
		mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.set(getMappedPost(post), SetOptions.merge())
				.addOnCompleteListener { }
				.addOnFailureListener {}
	}

	private fun getMappedPost(post : Post) : HashMap<String, Any> {
		val postMap = HashMap<String, Any>()
		postMap[FIRESTORE_POSTS_POST_BODY] = post.post!!
		postMap[FIRESTORE_POSTS_POST_AUTHOR_NAME] = post.authorName!!
		postMap[FIRESTORE_POSTS_POST_AUTHOR_ID] = post.authorUId!!
		postMap[FIRESTORE_POSTS_POST_AUTHOR_IMAGE] = post.authorImage!!
		postMap[FIRESTORE_POSTS_POST_CATEGORY_NAME] = post.categoryName!!
		postMap[FIRESTORE_POSTS_POST_DATE_CREATED] = Date()
		postMap[FIRESTORE_POSTS_POST_DOC_NAME] = post.documentName!!
		postMap[FIRESTORE_POSTS_POST_REGISTER_TOKEN] = post.registerToken!!

		return postMap
	}

	override fun getPostsFeed() : LiveData<List<Post>> {
		val posts = MutableLiveData<List<Post>>()
		mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
				.orderBy(FIRESTORE_POSTS_POST_DATE_CREATED, Query.Direction.DESCENDING)
				.addSnapshotListener { querySnapshot, e ->
					if (e != null) {

					} else {
						val documents = ArrayList<Post>()
						querySnapshot?.forEach { document ->
							documents.add(document.toObject(Post::class.java))
						}
						posts.value = documents
					}
				}
		return posts
	}

	override fun getCommentsFeed(documentName : String) : LiveData<List<Comment>> {
		val comments = MutableLiveData<List<Comment>>()
		mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
				.document(documentName)
				.addSnapshotListener { snapshot, e ->
					if (e != null) {

					} else {
						val commentList = ArrayList<Comment>()
						val post = snapshot?.toObject(Post::class.java)
						post?.comments?.forEach { comment ->
							if (!comment.isCommentEmpty()) {
								commentList.add(comment)
							}
						}
						comments.value = commentList
					}
				}
		return comments
	}


}