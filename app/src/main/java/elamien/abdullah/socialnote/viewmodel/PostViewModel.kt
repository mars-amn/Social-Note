package elamien.abdullah.socialnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import elamien.abdullah.socialnote.database.remote.firestore.models.Comment
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.database.remote.firestore.models.User
import elamien.abdullah.socialnote.repository.PostRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostViewModel : ViewModel(), KoinComponent {

    private val mPostRepository: PostRepository by inject()

    fun createPost(post: Post) {
        mPostRepository.createNewPost(post)
    }

    fun getPosts(): LiveData<List<Post>> {
        return mPostRepository.getPostsFeed()
    }

    fun createComment(postDocName: String, comment: Comment) {
        mPostRepository.createComment(postDocName, comment)
    }

    fun getComments(documentName: String): LiveData<List<Comment>> {
        return mPostRepository.getCommentsFeed(documentName)
    }

    fun createLikeOnPost(like: Like) {
        mPostRepository.createLikeOnPost(like)
    }

    fun removeLikePost(like: Like) { // aka Unlike
        mPostRepository.removeLike(like)
    }

    fun getUser(): LiveData<User> {
        return mPostRepository.getUser()
    }

    fun loadPost(documentName: String?): LiveData<Post> {
        return mPostRepository.loadPost(documentName)
    }
}