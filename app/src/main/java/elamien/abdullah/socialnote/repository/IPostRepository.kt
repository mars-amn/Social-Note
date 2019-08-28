package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import elamien.abdullah.socialnote.models.Comment
import elamien.abdullah.socialnote.models.Post

/**
 * Created by AbdullahAtta on 25-Aug-19.
 */
interface IPostRepository {

	fun createNewPost(post : Post)
	fun getPostsFeed() : LiveData<List<Post>>
	fun createComment(documentName : String, comment : Comment)
	fun getCommentsFeed(documentName : String) : LiveData<List<Comment>>
}