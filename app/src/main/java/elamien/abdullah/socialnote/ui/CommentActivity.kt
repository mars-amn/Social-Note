package elamien.abdullah.socialnote.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.CommentsAdapter
import elamien.abdullah.socialnote.database.remote.firestore.models.Comment
import elamien.abdullah.socialnote.database.remote.firestore.models.User
import elamien.abdullah.socialnote.databinding.ActivityCommentBinding
import elamien.abdullah.socialnote.receiver.NotificationReceiver
import elamien.abdullah.socialnote.utils.Constants.Companion.DISMISS_POST_COMMENT_NOTIFICATION_ACTION
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POST_DOC_INTENT_KEY
import elamien.abdullah.socialnote.utils.Constants.Companion.OPEN_FROM_NOTIFICATION_COMMENT
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CommentActivity : AppCompatActivity(), CommentsAdapter.CommentListener {

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
        }

        if (isPostFromNotification()) {
            dismissNotification()
        }
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


    private fun loadPostComments() {
        mPostViewModel.getComments(mDocumentName!!).observe(this, Observer { comments ->
            if (comments.isNotEmpty()) {
                mAdapter.mComments = comments
                mBinding.commentsRecyclerView.scrollToPosition(comments.size - 1)
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
