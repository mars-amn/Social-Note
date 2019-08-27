package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.CommentsAdapter
import elamien.abdullah.socialnote.databinding.ActivityCommentBinding
import elamien.abdullah.socialnote.models.Comment
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CommentActivity : AppCompatActivity() {
	private val mPostViewModel : PostViewModel by viewModel()
	private val mFirebaseAuth : FirebaseAuth by inject()
	private lateinit var mBinding : ActivityCommentBinding
	private var mDocumentName : String? = null
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this@CommentActivity, R.layout.activity_comment)
		mBinding.handlers = this

		if (intent != null && intent.hasExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY)) {
			mDocumentName = intent.getStringExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY)
			loadPostComments()
		}
	}

	private fun loadPostComments() {
		mPostViewModel.getComments(mDocumentName!!)
				.observe(this, Observer { comments ->
					if (comments.isNotEmpty()) {
						val adapter = CommentsAdapter(this@CommentActivity, comments)
						mBinding.commentsRecyclerView.adapter = adapter
						mBinding.commentsRecyclerView.scrollToPosition(comments.size - 1)
					}
				})
	}

	fun onSubmitButtonClick(view : View) {
		val commentBody = mBinding.commentInputEditText.text.toString()
		val authorName = mFirebaseAuth.currentUser?.displayName
		val authorUId = mFirebaseAuth.currentUser?.uid.toString()
		val authorImage = mFirebaseAuth.currentUser?.photoUrl.toString()

		val comment = Comment(commentBody, authorImage, authorName, authorUId, Timestamp(Date()))
		mPostViewModel.createComment(mDocumentName!!, comment)
	}
}
