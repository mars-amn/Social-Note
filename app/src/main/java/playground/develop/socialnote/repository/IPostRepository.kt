package playground.develop.socialnote.repository

import androidx.lifecycle.LiveData
import playground.develop.socialnote.database.remote.firestore.models.Comment
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User

/**
 * Created by AbdullahAtta on 25-Aug-19.
 */
interface IPostRepository {

    fun createNewPost(post: Post, countryCode: String)
    fun getPostsFeed(countryCode: String): LiveData<List<Post>>
    fun createComment(documentName: String, comment: Comment, countryCode: String)
    fun getCommentsFeed(documentName: String, countryCode: String?): LiveData<List<Comment>>
    fun createLikeOnPost(like: Like, countryCode: String?)
    fun removeLike(like: Like, countryCode: String)
    fun getUser(): LiveData<User>
    fun loadPost(documentName: String?, postCountryCode: String): LiveData<Post>
    fun getUser(userUid: String?): LiveData<User>
    fun getUserPosts(userUid: String?, countryCode: String): LiveData<List<Post>>
    fun getUserPosts(): LiveData<List<Post>>
    fun updateUser(user: User?)
    fun deleteComment(comment: Comment, countryCode: String)
    fun deletePost(post: Post)
    fun getPost(documentName: String?, countryCode: String?): LiveData<Post>
}