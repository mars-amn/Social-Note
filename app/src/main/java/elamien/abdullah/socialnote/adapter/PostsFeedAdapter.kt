package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.transitionseverywhere.extra.Scale
import elamien.abdullah.socialnote.R

import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.databinding.ListItemFeedBinding
import elamien.abdullah.socialnote.ui.CommentActivity
import elamien.abdullah.socialnote.utils.Constants
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject


/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostsFeedAdapter(private val listener : LikeClickListener,
					   private val context : Context,
					   private val mPostsFeed : List<Post>) :
	RecyclerView.Adapter<PostsFeedAdapter.PostsFeedViewHolder>(), KoinComponent {

	override fun onBindViewHolder(holder : PostsFeedViewHolder, position : Int) {
		holder.bind(mPostsFeed[position])
	}

	override fun getItemCount() : Int = mPostsFeed.size

	private val mFirebaseAuth : FirebaseAuth by inject()


	private val mDisposables = CompositeDisposable()

	init {
		getRegisterToken()

	}


	interface LikeClickListener {
		fun onLikeButtonClick(like : Like)
	}

	private var mRegisterToken : String? = null
	private fun getRegisterToken() {
		FirebaseInstanceId.getInstance()
				.instanceId.addOnSuccessListener { instanceIdResult ->
			mRegisterToken = instanceIdResult.token
		}
	}

	override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PostsFeedViewHolder {
		val inflater = LayoutInflater.from(context)
		val binding = ListItemFeedBinding.inflate(inflater, parent, false)
		return PostsFeedViewHolder(binding)
	}

	fun dispose() {
		mDisposables.dispose()
	}

	inner class PostsFeedViewHolder(private val mBinding : ListItemFeedBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		init {
			mBinding.handlers = this
		}

		fun bind(post : Post) {
			applyNormalStateOnLikeText()
			mBinding.post = post
		}

		private fun applyNormalStateOnLikeText() {
			mBinding.listItemFeedUpvoteButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(
					context,
					R.drawable.ic_like), null, null, null)
			mBinding.listItemFeedUpvoteButton.setTextColor(ContextCompat.getColor(context,
					R.color.normal_like_state_color))
		}

		private fun applyLikeStateOnLikeText() {
			applyAnimation()
			mBinding.listItemFeedUpvoteButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(
					context,
					R.drawable.ic_liked), null, null, null)
			mBinding.listItemFeedUpvoteButton.setTextColor(ContextCompat.getColor(context,
					R.color.liked_state_color))
		}

		private fun applyAnimation() {
			mBinding.listItemFeedUpvoteButton.visibility = View.INVISIBLE
			val set = TransitionSet().addTransition(Scale(0.7f))
					.addTransition(Fade())
					.setInterpolator(FastOutLinearInInterpolator())
			TransitionManager.beginDelayedTransition(mBinding.listItemFeedPostParent, set)

			mBinding.listItemFeedUpvoteButton.visibility = View.VISIBLE
		}

		fun onUpvoteButtonClick(view : View) {
			val post = mPostsFeed[adapterPosition]
			val like = Like(mFirebaseAuth.currentUser?.uid,
					post.registerToken,
					mRegisterToken,
					mFirebaseAuth.currentUser?.displayName,
					post.documentName,
					mFirebaseAuth.currentUser?.photoUrl.toString())
			listener.onLikeButtonClick(like)
			applyLikeStateOnLikeText()
		}

		fun onCommentButtonClick(view : View) {
			val post = mPostsFeed[adapterPosition]
			val intent = Intent(context, CommentActivity::class.java)
			intent.putExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY, post.documentName)
			intent.putExtra(Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY, post.registerToken)
			context.startActivity(intent)
		}
	}
}
