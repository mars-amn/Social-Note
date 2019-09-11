package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import elamien.abdullah.socialnote.database.remote.firestore.models.Comment
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.database.remote.firestore.models.User

/**
 * Created by AbdullahAtta on 25-Aug-19.
 */
interface IPostRepository {

    fun createNewPost(post: Post)
    fun getPostsFeed(): LiveData<List<Post>>
    fun createComment(documentName: String, comment: Comment)
    fun getCommentsFeed(documentName: String): LiveData<List<Comment>>
    fun createLikeOnPost(like: Like)
    fun removeLike(like: Like)
    fun getUser(): LiveData<User>
    fun loadPost(documentName: String?): LiveData<Post>
    fun getUser(userUid: String?): LiveData<User>
    fun getUserPosts(userUid: String?): LiveData<List<Post>>
    fun getUserPosts(): LiveData<List<Post>>
    fun updateUser(user: User?)
}