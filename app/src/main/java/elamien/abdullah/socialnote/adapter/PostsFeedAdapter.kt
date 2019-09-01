package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.text.HtmlCompat
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
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.math.ln
import kotlin.math.pow


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
			mBinding.listItemFeedBodyText.text = getPost(post.post!!)

			if (likedArray.contains(post.documentName!!)) {
				showLikedButton()
			}
			if (post.likes != null) {
				setupLikesCounter(numberCalculation(post.likes!!.size))
				post.likes?.forEach { like ->
					if (like.userLikerUId == mFirebaseAuth.currentUser?.uid || likedArray.contains(
								post.documentName!!)) {
						showLikedButton()
					} else {
						hideLikedButton()
					}
				}
			} else {
				hideLikeCounter()
			}
		}

		private fun hideLikeCounter() {
			applyAnimation()
			mBinding.listItemLikesCounter.text = "0"
		}

		private fun setupLikesCounter(count : String) {
			mBinding.listItemLikesCounter.text = count
			applyAnimation()
			mBinding.listItemLikesCounter.visibility = View.VISIBLE
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
			if (post.likes != null) {
				setupLikesCounter(numberCalculation(post.likes!!.size.plus(1)))
			} else {
				setupLikesCounter("1")
			}
			val like = Like(mFirebaseAuth.currentUser?.uid,

					post.registerToken,
					mRegisterToken,
					mFirebaseAuth.currentUser?.displayName,
					post.documentName,
					mFirebaseAuth.currentUser?.photoUrl.toString())
			listener.onLikeButtonClick(like)
			likedArray.add(post.documentName!!)
		}

		private fun numberCalculation(number : Int) : String {
			if (number < 1000) return "" + number
			val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
			return String.format("%.1f %c", number / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
		}

		fun onUnLikeButtonClick(view : View) {
			hideLikedButton()
			val post = mPostsFeed[adapterPosition]
			if (post.likes != null) {
				setupLikesCounter(numberCalculation(mBinding.listItemLikesCounter.text.toString().toInt().minus(
						1)))
			} else {
				hideLikeCounter()
			}
			val like = Like(mFirebaseAuth.currentUser?.uid,
					post.registerToken,
					mRegisterToken,
					mFirebaseAuth.currentUser?.displayName,
					post.documentName,
					mFirebaseAuth.currentUser?.photoUrl.toString())
			listener.onUnLikeButtonClick(like)
			likedArray.remove(post.documentName!!)
		}

		fun onSharePostClick(view : View) {
			val post = mPostsFeed[adapterPosition]

			ShareCompat.IntentBuilder.from(context as AppCompatActivity)
					.setType("text/plain")
					.setText("Checkout what ${post.authorName} posted on Social Note \n\n" + "${getPost(
							post.post!!)}")
					.setChooserTitle(context.getString(R.string.share_title))
					.startChooser()
		}

		@Suppress("DEPRECATION")
		private fun getPost(body : String?) : Spanned {
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				HtmlCompat.fromHtml("$body ...", HtmlCompat.FROM_HTML_MODE_COMPACT)
				//or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE or HtmlCompat.FROM_HTML_MODE_COMPACT)
			} else {
				Html.fromHtml(body)
			}
		}
	}
}


