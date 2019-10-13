package playground.develop.socialnote.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import org.greenrobot.eventbus.EventBus
import org.koin.core.KoinComponent
import org.koin.core.inject
import playground.develop.socialnote.BuildConfig
import playground.develop.socialnote.database.remote.firestore.models.Comment
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.eventbus.SocialPostMessage
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.BLOCKED_EVENT
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_IMAGE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_REGISTER_TOKEN
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_UID
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COLLECTION_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COMMENT
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_COMMENTER_AUTHOR_TOKEN
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_NOTIFICATION_DATE_CREATED
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_POST_COMMENT_AUTHOR_COUNTRY_CODE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COMMENTS_POST_COMMENT_DOC_ID
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_AUTHOR_REGISTER_TOKEN
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_COLLECTION_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_COUNTRY_CODE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_DOCUMENT_ID
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_USER_ID
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_USER_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_LIKES_NOTIFICATION_USER_TOKEN
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_COLLECTION_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_ID
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_IMAGE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_AUTHOR_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_BODY
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_CATEGORY_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_COMMENTS
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_DATE_CREATED
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_DOC_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_IMAGE_URL
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_LIKES
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POSTS_POST_REGISTER_TOKEN
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_USERS_COLLECTION_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_USER_COUNTRY_CODE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_USER_POSTS_COUNT
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_USER_TITLE
import playground.develop.socialnote.utils.Constants.Companion.MINIMUM_AUTHOR_TITLE_POSTS
import playground.develop.socialnote.utils.Constants.Companion.MINIMUM_ORIGINATOR_TITLE_POSTS
import playground.develop.socialnote.utils.Constants.Companion.ORIGINATOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.POST_SUCCESS_EVENT
import playground.develop.socialnote.utils.Constants.Companion.READER_TITLE
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostRepository : IPostRepository, KoinComponent {


    private val mFirestore: FirebaseFirestore by inject()
    private val mAuth: FirebaseAuth by inject()

    override fun getPost(documentName: String?, countryCode: String?): LiveData<Post> {
        val post = MutableLiveData<Post>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode!!))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(documentName!!).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    post.value = task.result?.toObject(Post::class.java)
                }
            }
        return post
    }

    override fun deletePost(post: Post) {
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(post.countryCode!!))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(post.documentName!!).delete()
    }

    override fun deleteComment(comment: Comment, countryCode: String) {
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(comment.documentId!!)
            .update(FIRESTORE_POSTS_POST_COMMENTS, FieldValue.arrayRemove(comment))
            .addOnCompleteListener { }.addOnFailureListener { }
    }

    override fun removeLike(like: Like, countryCode: String) {
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(like.documentId!!)
            .update(FIRESTORE_POSTS_POST_LIKES, FieldValue.arrayRemove(like))
            .addOnCompleteListener { }.addOnFailureListener { }
    }

    override fun getUserPosts(): LiveData<List<Post>> {
        val posts = MutableLiveData<List<Post>>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .whereEqualTo(FIRESTORE_POSTS_POST_AUTHOR_ID, mAuth.currentUser?.uid)
            .orderBy(FIRESTORE_POSTS_POST_DATE_CREATED, Query.Direction.DESCENDING).get()
            .addOnCompleteListener { querySnapshot ->
                if (querySnapshot.isSuccessful) {
                    val postsList = ArrayList<Post>()
                    querySnapshot.result?.forEach { document ->
                        postsList.add(document.toObject(Post::class.java))
                    }
                    posts.value = postsList
                }
            }.addOnFailureListener { }
        return posts
    }

    override fun getUserPosts(userUid: String?, countryCode: String): LiveData<List<Post>> {
        val posts = MutableLiveData<List<Post>>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .whereEqualTo(FIRESTORE_POSTS_POST_AUTHOR_ID, userUid)
            .orderBy(FIRESTORE_POSTS_POST_DATE_CREATED, Query.Direction.DESCENDING).get()
            .addOnCompleteListener { querySnapshot ->
                if (querySnapshot.isSuccessful) {
                    val postsList = ArrayList<Post>()
                    querySnapshot.result?.forEach { document ->
                        postsList.add(document.toObject(Post::class.java))
                    }
                    posts.value = postsList
                } else {

                }
            }.addOnFailureListener { }
        return posts
    }

    override fun getUser(userUid: String?): LiveData<User> {
        val user = MutableLiveData<User>()
        mFirestore.collection(FIRESTORE_USERS_COLLECTION_NAME).document(userUid!!).get()
            .addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    user.value = document.result?.toObject(User::class.java)
                }
            }
        return user
    }

    override fun getUser(): LiveData<User> {
        val user = MutableLiveData<User>()
        mFirestore.collection(FIRESTORE_USERS_COLLECTION_NAME).document(mAuth.currentUser?.uid!!)
            .get().addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    user.value = document.result?.toObject(User::class.java)
                }
            }
        return user
    }

    override fun loadPost(documentName: String?, postCountryCode: String): LiveData<Post> {
        val post = MutableLiveData<Post>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(postCountryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(documentName!!).get()
            .addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    post.value = document.result?.toObject(Post::class.java)
                }
            }
        return post
    }


    override fun createLikeOnPost(like: Like, countryCode: String?) {
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode!!))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(like.documentId!!)
            .update(FIRESTORE_POSTS_POST_LIKES, FieldValue.arrayUnion(like))
            .addOnCompleteListener { }.addOnFailureListener { }

        mFirestore.collection(FIRESTORE_LIKES_NOTIFICATION_COLLECTION_NAME).document()
            .set(getMappedLike(like, countryCode)).addOnCompleteListener { }.addOnFailureListener {}
    }

    private fun getMappedLike(like: Like, countryCode: String): HashMap<String, Any> {
        val likeMap = HashMap<String, Any>()
        likeMap[FIRESTORE_LIKES_NOTIFICATION_AUTHOR_REGISTER_TOKEN] = like.authorRegisterToken!!
        likeMap[FIRESTORE_LIKES_NOTIFICATION_USER_TOKEN] = like.userRegisterToken!!
        likeMap[FIRESTORE_LIKES_NOTIFICATION_USER_NAME] = like.userName!!
        likeMap[FIRESTORE_LIKES_NOTIFICATION_DOCUMENT_ID] = like.documentId!!
        likeMap[FIRESTORE_LIKES_NOTIFICATION_USER_ID] = like.userLikerUId!!
        likeMap[FIRESTORE_LIKES_NOTIFICATION_COUNTRY_CODE] = countryCode
        likeMap[Constants.EXTRA_DATABASE_FIELD] = ""
        likeMap[Constants.EXTRA_DATABASE_FIELD_ONE] = ""
        likeMap[Constants.EXTRA_DATABASE_FIELD_TWO] = ""
        likeMap[Constants.EXTRA_DATABASE_FIELD_THREE] = ""
        likeMap[Constants.EXTRA_DATABASE_FIELD_FOUR] = false
        likeMap[Constants.EXTRA_DATABASE_FIELD_FIVE] = false
        return likeMap
    }

    override fun createComment(documentName: String, comment: Comment, countryCode: String) {
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(documentName)
            .update(FIRESTORE_POSTS_POST_COMMENTS, FieldValue.arrayUnion(comment))
            .addOnCompleteListener { }.addOnFailureListener { }

        mFirestore.collection(FIRESTORE_COMMENTS_NOTIFICATION_COLLECTION_NAME).document()
            .set(getMappedComment(comment, countryCode)).addOnCompleteListener { }
            .addOnFailureListener { }
    }

    private fun getMappedComment(comment: Comment, countryCode: String): HashMap<String, Any> {
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
        commentMap[FIRESTORE_COMMENTS_POST_COMMENT_AUTHOR_COUNTRY_CODE] = countryCode
        commentMap[FIRESTORE_USER_TITLE] = comment.authorTitle!!
        commentMap[Constants.EXTRA_DATABASE_FIELD] = ""
        commentMap[Constants.EXTRA_DATABASE_FIELD_ONE] = ""
        commentMap[Constants.EXTRA_DATABASE_FIELD_TWO] = ""
        commentMap[Constants.EXTRA_DATABASE_FIELD_THREE] = ""
        commentMap[Constants.EXTRA_DATABASE_FIELD_FOUR] = false
        commentMap[Constants.EXTRA_DATABASE_FIELD_FIVE] = false
        return commentMap
    }

    override fun createNewPost(post: Post, countryCode: String) {
        val documentName = "${post.authorUID}${Date().time}"
        post.documentName = documentName
        mFirestore.collection(FIRESTORE_USERS_COLLECTION_NAME).document(mAuth.currentUser?.uid!!)

            .get().addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    val user = document.result?.toObject(User::class.java)
                    user?.userPostsCount = user?.userPostsCount!!.plus(1)
                    var title = READER_TITLE
                    if (user.userPostsCount!! >= MINIMUM_ORIGINATOR_TITLE_POSTS) {
                        title = ORIGINATOR_TITLE
                    } else if (user.userPostsCount!! >= MINIMUM_AUTHOR_TITLE_POSTS) {
                        title = AUTHOR_TITLE
                    }
                    post.userTitle = title
                    user.userTitle = title

                    mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
                        .document(getPostCollectionName(countryCode))
                        .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(documentName)
                        .set(getMappedPost(post), SetOptions.merge()).addOnSuccessListener {
                            postPostEvent(POST_SUCCESS_EVENT)
                            updateUser(user)
                        }.addOnFailureListener {
                            val firestoreException = it as? FirebaseFirestoreException
                            if (firestoreException!!.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                postPostEvent(BLOCKED_EVENT)
                            }
                        }
                }
            }


    }

    private fun postPostEvent(event: String) {
        EventBus.getDefault().post(SocialPostMessage(event))
    }

    override fun updateUser(user: User?) {
        mFirestore.collection(FIRESTORE_USERS_COLLECTION_NAME).document(user?.userUid!!)
            .update(getMappedUser(user = user))
    }

    private fun getMappedUser(user: User): HashMap<String, Any> {
        val userMap = HashMap<String, Any>()
        userMap[Constants.FIRESTORE_USER_UID] = user.userUid!!
        userMap[Constants.FIRESTORE_USER_IMAGE_URL] = user.userImage!!
        userMap[Constants.FIRESTORE_USER_NAME] = user.userName!!
        userMap[FIRESTORE_USER_TITLE] = user.userTitle!!
        userMap[FIRESTORE_USER_POSTS_COUNT] = user.userPostsCount!!
        userMap[Constants.FIRESTORE_USER_COVER_IMAGE] = user.coverImage!!
        return userMap
    }

    private fun decreaseUserPostsCount(authorUId: String?) {
        mFirestore.collection(FIRESTORE_USERS_COLLECTION_NAME).document(authorUId!!)
            .update(FIRESTORE_USER_POSTS_COUNT, FieldValue.increment(-1))
    }

    private fun getMappedPost(post: Post): HashMap<String, Any> {
        val postMap = HashMap<String, Any>()
        postMap[FIRESTORE_POSTS_POST_BODY] = post.post!!
        postMap[FIRESTORE_POSTS_POST_AUTHOR_NAME] = post.authorName!!
        postMap[FIRESTORE_POSTS_POST_AUTHOR_ID] = post.authorUID!!
        postMap[FIRESTORE_POSTS_POST_AUTHOR_IMAGE] = post.authorImage!!
        postMap[FIRESTORE_POSTS_POST_CATEGORY_NAME] = post.categoryName!!
        postMap[FIRESTORE_POSTS_POST_DATE_CREATED] = Date()
        postMap[FIRESTORE_POSTS_POST_DOC_NAME] = post.documentName!!
        postMap[FIRESTORE_POSTS_POST_REGISTER_TOKEN] = post.registerToken!!
        postMap[FIRESTORE_USER_TITLE] = post.userTitle!!
        postMap[FIRESTORE_USER_COUNTRY_CODE] = post.countryCode!!
        postMap[Constants.EXTRA_DATABASE_FIELD] = ""
        postMap[Constants.EXTRA_DATABASE_FIELD_ONE] = ""
        postMap[Constants.EXTRA_DATABASE_FIELD_TWO] = ""
        postMap[Constants.EXTRA_DATABASE_FIELD_THREE] = ""
        postMap[Constants.EXTRA_DATABASE_FIELD_FOUR] = false
        postMap[Constants.EXTRA_DATABASE_FIELD_FIVE] = false
        if (post.imageUrl != null) {
            postMap[FIRESTORE_POSTS_POST_IMAGE_URL] = post.imageUrl!!
        }
        return postMap
    }

    override fun getPostsFeed(countryCode: String): LiveData<List<Post>> {
        val posts = MutableLiveData<List<Post>>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .orderBy(FIRESTORE_POSTS_POST_DATE_CREATED, Query.Direction.DESCENDING).get()
            .addOnCompleteListener { querySnapshot ->
                if (querySnapshot.isSuccessful) {
                    val postsList = ArrayList<Post>()
                    querySnapshot.result?.forEach { document ->
                        postsList.add(document.toObject(Post::class.java))
                    }
                    posts.value = postsList
                }
            }
        return posts
    }

    private fun getPostCollectionName(countryCode: String) =
        "$FIRESTORE_POSTS_COLLECTION_NAME-$countryCode"

    override fun getCommentsFeed(documentName: String, countryCode: String?): LiveData<List<Comment>> {
        val comments = MutableLiveData<List<Comment>>()
        mFirestore.collection(FIRESTORE_POSTS_COLLECTION_NAME)
            .document(getPostCollectionName(countryCode!!))
            .collection(FIRESTORE_POSTS_COLLECTION_NAME).document(documentName)
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

    fun bid(pid: String?, h: String) {
        mFirestore.collection(BuildConfig.b_f).document(pid!!).set(bmap(pid, h))
    }

    private fun bmap(pid: String?, h: String): HashMap<String, Any> {
        val bidmap = HashMap<String, Any>()
        bidmap[BuildConfig.B_F_L] = pid!!
        bidmap[BuildConfig.B_F_H] = h
        return bidmap
    }

}