package playground.develop.socialnote.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import playground.develop.socialnote.R
import playground.develop.socialnote.adapter.CommentsAdapter
import playground.develop.socialnote.database.remote.firestore.models.Comment
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.databinding.ActivityCommentBinding
import playground.develop.socialnote.receiver.NotificationReceiver
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.DISMISS_POST_COMMENT_NOTIFICATION_ACTION
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POST_DOC_INTENT_KEY
import playground.develop.socialnote.utils.Constants.Companion.OPEN_FROM_NOTIFICATION_COMMENT
import playground.develop.socialnote.utils.Constants.Companion.READER_TITLE
import playground.develop.socialnote.viewmodel.PostViewModel
import java.util.*
import java.util.Collections.reverse
import kotlin.math.ln
import kotlin.math.pow

class CommentActivity : AppCompatActivity(), CommentsAdapter.CommentListener {

    private var mPost: Post? = null
    private val mPostViewModel: PostViewModel by viewModel()
    private val mFirebaseAuth: FirebaseAuth by inject()
    private lateinit var mBinding: ActivityCommentBinding
    private var mDocumentName: String? = null
    private var mAuthorRegisterToken: String? = null
    private lateinit var mAdapter: CommentsAdapter
    private lateinit var mUser: User
    private var mRegisterToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@CommentActivity, R.layout.activity_comment)
        mBinding.handlers = this

        if (intent != null && intent.hasExtra(FIRESTORE_POST_DOC_INTENT_KEY) && intent.hasExtra(
                FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY)) {
            loadUser()
            mDocumentName = intent.getStringExtra(FIRESTORE_POST_DOC_INTENT_KEY)
            mAuthorRegisterToken = intent.getStringExtra(FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY)
            mAdapter = CommentsAdapter(this@CommentActivity, this@CommentActivity)
            mBinding.commentsRecyclerView.adapter = mAdapter
            loadRegistrationToken()
            loadPostComments()
            loadPost()
        }

        if (isPostFromNotification()) {
            dismissNotification()
        }
    }

    private fun loadPost() {
        mPostViewModel.getPost(mDocumentName).observe(this, Observer { post ->
            if (post != null) {
                mPost = post
                showPost(post)
            }
        })
    }

    private fun showPost(post: Post) {

        bindPostBody(post.post)
        bindAuthorImage(post.authorImage!!)
        bindAuthorName(post.authorName)
        bindPostDate(post.getDateCreated())
        bindAuthorTitle(post.userTitle)


        if (post.likes != null) {
            val likes = post.likes
            setupLikesCounter(likes?.size)
            setupLikeButton(likes)
        } else {
            setLikesCounterTo0()
        }

    }

    private fun setLikesCounterTo0() {
        mBinding.postLikesCounter.text = "0"
    }

    private fun setupLikesCounter(likesCount: Int?) {
        mBinding.postLikesCounter.text = numberCalculation(likesCount!!)
    }

    private fun bindAuthorTitle(userTitle: String?) {
        when (userTitle) {
            READER_TITLE -> showReaderTitle()
            AUTHOR_TITLE -> showAuthorTitle()
        }
    }

    private fun numberCalculation(number: Int): String {
        if (number < 1000) return "" + number
        val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
        return String.format("%.1f %c", number / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
    }

    private fun showAuthorTitle() {
        mBinding.postAuthorTitle.visibility = View.VISIBLE
    }

    private fun showReaderTitle() {
        mBinding.postReaderTitle.visibility = View.VISIBLE
    }

    private fun bindPostDate(dateCreated: Date) {
        mBinding.postDate.text = DateUtils.getRelativeTimeSpanString(dateCreated.time)
    }

    private fun bindAuthorName(authorName: String?) {
        mBinding.postAuthorName.text = authorName
    }

    private fun bindAuthorImage(imageUrl: String) {
        mBinding.postAuthorImage.load(imageUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private fun bindPostBody(post: String?) {
        mBinding.postBodyText.setHtml(post!!, HtmlHttpImageGetter(mBinding.postBodyText))
    }

    private fun setupLikeButton(likes: ArrayList<Like>?) {
        likes?.forEach { like ->
            if (like.userLikerUId == mFirebaseAuth.currentUser?.uid) {
                showUnlikeButton()
                return@forEach
            }
        }
    }

    fun onPostLongClick(view: View) {
        toast("longClick")
    }

    fun onUserImageClick(view: View) {
        val userUid = mFirebaseAuth.currentUser?.uid
        startActivity(intentFor<ProfileActivity>(Constants.USER_UID_INTENT_KEY to userUid))
    }

    fun onLikesCounterClick(view: View) {
        startActivity(intentFor<LikesActivity>(Constants.USER_LIKES_INTENT_KEY to mPost!!.documentName))
    }

    fun onLikeButtonClick(view: View) {
        val like = Like(mFirebaseAuth.currentUser?.uid,
                        mPost!!.registerToken,
                        mRegisterToken,
                        mFirebaseAuth.currentUser?.displayName,
                        mPost!!.documentName,
                        mFirebaseAuth.currentUser?.photoUrl.toString())
        mPostViewModel.createLikeOnPost(like)
        showUnlikeButton()
    }


    private fun showLikeButton() {
        mBinding.postUnLikeButton.visibility = View.GONE
        mBinding.postLikeButton.visibility = View.VISIBLE
    }

    private fun showUnlikeButton() {
        mBinding.postLikeButton.visibility = View.GONE
        mBinding.postUnLikeButton.visibility = View.VISIBLE
    }

    fun onUnLikeButtonClick(view: View) {
        val like = Like(mFirebaseAuth.currentUser?.uid,
                        mPost!!.registerToken,
                        mRegisterToken,
                        mFirebaseAuth.currentUser?.displayName,
                        mPost!!.documentName,
                        mFirebaseAuth.currentUser?.photoUrl.toString())
        mPostViewModel.removeLikePost(like)
        showLikeButton()
    }

    fun onCommentButtonClick(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(mBinding.commentInputEditText, SHOW_IMPLICIT)
    }

    fun onSharePostClick(view: View) {

    }

    private fun loadUser() {
        mPostViewModel.getUser().observe(this@CommentActivity, Observer { user ->
            mUser = user

        })
    }

    private fun dismissNotification() {
        val dismissIntent = Intent(this@CommentActivity, NotificationReceiver::class.java)
        dismissIntent.action = DISMISS_POST_COMMENT_NOTIFICATION_ACTION
        dismissIntent.putExtra(DISMISS_POST_COMMENT_NOTIFICATION_ACTION,
                               intent.getIntExtra(DISMISS_POST_COMMENT_NOTIFICATION_ACTION, -1))
        PendingIntent.getBroadcast(this@CommentActivity,
                                   0,
                                   dismissIntent,
                                   PendingIntent.FLAG_UPDATE_CURRENT)
        sendBroadcast(dismissIntent)
    }

    private fun isPostFromNotification(): Boolean = intent.getBooleanExtra(
        OPEN_FROM_NOTIFICATION_COMMENT,
        false)


    private var oldCommentsSize = 0
    private fun loadPostComments() {
        mPostViewModel.getComments(mDocumentName!!).observe(this, Observer { comments ->
            if (comments.isNotEmpty()) {
                if (oldCommentsSize == comments.size) {

                } else {
                    reverse(comments)
                    mAdapter.mComments = comments
                    if (comments[0].authorUId == mFirebaseAuth.currentUser?.uid) {
                        mBinding.commentsRecyclerView.scrollToPosition(0)
                    }
                    oldCommentsSize = comments.size
                }
            }
        })
    }

    fun onSubmitButtonClick(view: View) {
        val commentBody = mBinding.commentInputEditText.text.toString()
        if (commentBody == "") return

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        val authorName = mFirebaseAuth.currentUser?.displayName
        val authorUId = mFirebaseAuth.currentUser?.uid.toString()
        val authorImage = mUser.userImage

        val comment = Comment(mRegisterToken,
                              mAuthorRegisterToken,
                              mDocumentName,
                              commentBody,
                              authorImage,
                              authorName,
                              authorUId,
                              Timestamp(Date()),
                              mUser.userTitle)
        mPostViewModel.createComment(mDocumentName!!, comment)
        mBinding.commentInputEditText.setText("")
    }

    private fun loadRegistrationToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            mRegisterToken = instanceIdResult.token
        }
    }

    override fun onCommentLongClick(comment: Comment) {
        if (comment.authorUId == mUser.userUid) {
            MaterialAlertDialogBuilder(this@CommentActivity)
                    .setTitle(getString(R.string.delete_author_comment_dialog_title))
                    .setMessage(getString(R.string.delete_author_comment_dialog_message))
                    .setNegativeButton(getString(R.string.delete_author_comment_dialog_negative_button)) { dialog, id ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.delete_author_comment_dialog_positive_button)) { dialog, id ->
                        mPostViewModel.deleteComment(comment)
                        dialog.dismiss()
                    }.show()
        }
    }
}
