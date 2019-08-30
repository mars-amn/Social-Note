package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.transitionseverywhere.extra.Scale
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.databinding.ListItemFeedBinding
import elamien.abdullah.socialnote.ui.CommentActivity
import elamien.abdullah.socialnote.utils.Constants
import org.koin.core.KoinComponent
import org.koin.core.inject


/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostsFeedAdapter(private val listener : LikeClickListener,
					   private val context : Context,
					   private var mPostsFeed : List<Post>) :
	RecyclerView.Adapter<PostsFeedAdapter.PostsFeedViewHolder>(), KoinComponent {

	private val mFirebaseAuth : FirebaseAuth by inject()
	val likedArray = ArrayList<String>()

	init {
		getRegisterToken()

	}

	override fun onBindViewHolder(holder : PostsFeedViewHolder, position : Int) {
		holder.bind(mPostsFeed[position])
	}

	override fun getItemCount() : Int = mPostsFeed.size


	interface LikeClickListener {
		fun onLikeButtonClick(like : Like)
		fun onUnLikeButtonClick(like : Like)
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

	fun addPosts(posts : List<Post>?) {
		mPostsFeed = posts!!
		notifyDataSetChanged()
	}

	inner class PostsFeedViewHolder(private val mBinding : ListItemFeedBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		init {
			mBinding.handlers = this
		}

		fun bind(post : Post) {
			hideLikedButton()
			mBinding.post = post

			if (post.likes != null) {
				if (likedArray.contains(post.documentName!!)) {
					showLikedButton()
				} else {
					post.likes?.forEach { like ->
						if (like.userLikerUId == mFirebaseAuth.currentUser?.uid || likedArray.contains(
									post.documentName!!)) {
							showLikedButton()
						} else {
							hideLikedButton()
						}
					}
				}
			}
		}

		private fun hideLikedButton() {
			applyAnimation()
			mBinding.listItemFeedLikeButton.visibility = View.VISIBLE
			mBinding.listItemFeedUnLikeButton.visibility = View.GONE
		}

		private fun showLikedButton() {
			applyAnimation()
			mBinding.listItemFeedUnLikeButton.visibility = View.VISIBLE
			mBinding.listItemFeedLikeButton.visibility = View.GONE
		}

		private fun applyAnimation() {
			val set = TransitionSet().addTransition(Scale(0.7f))
					.addTransition(Fade())
					.setInterpolator(FastOutLinearInInterpolator())
			TransitionManager.beginDelayedTransition(mBinding.listItemFeedPostParent, set)
		}


		fun onCommentButtonClick(view : View) {
			val post = mPostsFeed[adapterPosition]
			val intent = Intent(context, CommentActivity::class.java)
			intent.putExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY, post.documentName)
			intent.putExtra(Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY, post.registerToken)
			context.startActivity(intent)
		}

		fun onLikeButtonClick(view : View) {
			showLikedButton()
			val post = mPostsFeed[adapterPosition]
			val like = Like(mFirebaseAuth.currentUser?.uid,

					post.registerToken,
					mRegisterToken,
					mFirebaseAuth.currentUser?.displayName,
					post.documentName,
					mFirebaseAuth.currentUser?.photoUrl.toString())
			listener.onLikeButtonClick(like)
			likedArray.add(post.documentName!!)
		}

		fun onUnLikeButtonClick(view : View) {
			hideLikedButton()
			val post = mPostsFeed[adapterPosition]
			val like = Like(mFirebaseAuth.currentUser?.uid,
					post.registerToken,
					mRegisterToken,
					mFirebaseAuth.currentUser?.displayName,
					post.documentName,
					mFirebaseAuth.currentUser?.photoUrl.toString())
			listener.onUnLikeButtonClick(like)
			likedArray.remove(post.documentName!!)
		}
	}
}


