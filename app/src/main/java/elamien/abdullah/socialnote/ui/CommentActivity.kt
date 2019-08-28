package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
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
	private var mAuthorRegisterToken : String? = null
	private lateinit var mAdapter : CommentsAdapter
	private var mRegisterToken : String? = null


	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this@CommentActivity, R.layout.activity_comment)
		mBinding.handlers = this

		if (intent != null && intent.hasExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY) && intent.hasExtra(
					Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY)) {
			mDocumentName = intent.getStringExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY)
			mAuthorRegisterToken =
				intent.getStringExtra(Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY)
			mAdapter = CommentsAdapter(this@CommentActivity, ArrayList())
			mBinding.commentsRecyclerView.adapter = mAdapter
			getRegisterToken()
			loadPostComments()
		}
	}

	private fun loadPostComments() {
		mPostViewModel.getComments(mDocumentName!!)
				.observe(this, Observer { comments ->
					if (comments.isNotEmpty()) {
						mAdapter.addComments(comments)
						mBinding.commentsRecyclerView.scrollToPosition(comments.size - 1)
					}
				})
	}

	fun onSubmitButtonClick(view : View) {
		val commentBody = mBinding.commentInputEditText.text.toString()
		if (commentBody == "") return

		val authorName = mFirebaseAuth.currentUser?.displayName
		val authorUId = mFirebaseAuth.currentUser?.uid.toString()
		val authorImage = mFirebaseAuth.currentUser?.photoUrl.toString()

		val comment = Comment(mRegisterToken,
				mAuthorRegisterToken,
				mDocumentName,
				commentBody,
				authorImage,
				authorName,
				authorUId,
				Timestamp(Date()))
		mPostViewModel.createComment(mDocumentName!!, comment)
		mBinding.commentInputEditText.setText("")
	}

	private fun getRegisterToken() {
		FirebaseInstanceId.getInstance()
				.instanceId.addOnSuccessListener { instanceIdResult ->
			mRegisterToken = instanceIdResult.token
		}
	}
}
