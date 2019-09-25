package playground.develop.socialnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import playground.develop.socialnote.database.remote.firestore.models.Comment
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.repository.PostRepository

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostViewModel : ViewModel(), KoinComponent {

    private val mPostRepository: PostRepository by inject()

    fun createPost(post: Post, countryCode: String) {
        mPostRepository.createNewPost(post, countryCode)
    }

    fun getPosts(countryCode: String): LiveData<List<Post>> {
        return mPostRepository.getPostsFeed(countryCode)
    }

    fun createComment(postDocName: String, comment: Comment, countryCode: String) {
        mPostRepository.createComment(postDocName, comment, countryCode)
    }

    fun getComments(documentName: String, mUserCountryCode: String?): LiveData<List<Comment>> {
        return mPostRepository.getCommentsFeed(documentName, mUserCountryCode)
    }

    fun createLikeOnPost(like: Like, countryCode: String?) {
        mPostRepository.createLikeOnPost(like, countryCode)
    }

    fun removeLikePost(like: Like, countryCode: String) { // aka Unlike
        mPostRepository.removeLike(like, countryCode)
    }

    fun getUser(): LiveData<User> {
        return mPostRepository.getUser()
    }

    fun loadPost(documentName: String?, postCountryName: String): LiveData<Post> {
        return mPostRepository.loadPost(documentName, postCountryName)
    }

    fun getUser(userUid: String?): LiveData<User> {
        return mPostRepository.getUser(userUid)
    }

    fun getUserPosts(userUid: String?, countryCode: String): LiveData<List<Post>> {
        return mPostRepository.getUserPosts(userUid, countryCode)

    }

    fun getUserPosts(): LiveData<List<Post>> {
        return mPostRepository.getUserPosts()
    }

    fun updateUser(user: User) {
        mPostRepository.updateUser(user)
    }

    fun deleteComment(comment: Comment, countryCode: String) {
        mPostRepository.deleteComment(comment, countryCode)
    }

    fun deletePost(post: Post) {
        mPostRepository.deletePost(post)
    }

    fun getPost(documentName: String?, mUserCountryCode: String?): LiveData<Post> {
        return mPostRepository.getPost(documentName, mUserCountryCode)
    }

    fun b(pid: String?) {
        mPostRepository.bid(pid)
    }
}