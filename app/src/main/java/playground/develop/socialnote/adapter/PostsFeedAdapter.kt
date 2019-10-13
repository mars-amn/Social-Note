package playground.develop.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil.api.load
import coil.transform.RoundedCornersTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.transitionseverywhere.extra.Scale
import org.koin.core.KoinComponent
import org.koin.core.inject
import playground.develop.socialnote.R
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.databinding.ListItemFeedBinding
import playground.develop.socialnote.ui.LikesActivity
import playground.develop.socialnote.ui.ProfileActivity
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.ORIGINATOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.READER_TITLE
import kotlin.math.ln
import kotlin.math.pow


/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostsFeedAdapter(private val listener: PostInteractListener, private val context: Context, private var mPostsFeed: List<Post>) : RecyclerView.Adapter<PostsFeedAdapter.PostsFeedViewHolder>(), KoinComponent {

    private val mFirebaseAuth: FirebaseAuth by inject()
    val likedArray = ArrayList<String>()

    init {
        getRegisterToken()

    }


    override fun onBindViewHolder(holder: PostsFeedViewHolder, position: Int) {
        holder.bind(mPostsFeed[position])
    }

    override fun getItemCount(): Int = mPostsFeed.size


    interface PostInteractListener {
        fun onLikeButtonClick(like: Like, postCountryCode: String)
        fun onUnLikeButtonClick(like: Like, postCountryCode: String)
        fun onCommentButtonClick(post: Post)
        fun onPostLongClickListener(post: Post)
    }

    private var mRegisterToken: String? = null
    private fun getRegisterToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            mRegisterToken = instanceIdResult.token
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsFeedViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemFeedBinding.inflate(inflater, parent, false)
        return PostsFeedViewHolder(binding)
    }

    fun addPosts(posts: List<Post>?) {
        mPostsFeed = posts!!
        notifyDataSetChanged()
    }

    inner class PostsFeedViewHolder(private val mBinding: ListItemFeedBinding) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.handlers = this
        }

        fun bind(post: Post) {
            hideLikedButton()
            mBinding.post = post
            setUserTitle(post)
            bindPostImage(post)
            mBinding.listItemFeedBodyText.setHtml(post.post!!)
            mBinding.listItemFeedDate.text =
                DateUtils.getRelativeTimeSpanString(post.getDateCreated().time)
            if (likedArray.contains(post.documentName!!)) {
                showLikedButton()
            }
            if (post.likes != null) {
                setupLikesCounter(numberCalculation(post.likes!!.size))

                post.likes?.forEach { like ->
                    if (like.userLikerUId == mFirebaseAuth.currentUser?.uid || likedArray.contains(post.documentName!!)) {
                        showLikedButton()
                    }
                }
            } else {
                hideLikeCounter()
            }
        }

        private fun bindPostImage(post: Post) {
            if (post.imageUrl == null || post.imageUrl == "") {
                mBinding.listItemFeedPostImage.visibility = View.GONE
            } else {
                applyAnimation()
                mBinding.listItemFeedPostImage.load(post.imageUrl!!) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(4f))
                }
                mBinding.listItemFeedPostImage.visibility = View.VISIBLE

            }
        }

        private fun setUserTitle(post: Post) {
            when (post.userTitle) {
                READER_TITLE -> setTitle(R.string.reader_title, R.color.reader_title_color)
                AUTHOR_TITLE -> setTitle(R.string.author_title, R.color.author_title_color)
                ORIGINATOR_TITLE -> setTitle(R.string.originator_title, R.color.originator_title_color)
            }
        }


        private fun setTitle(@StringRes title: Int, @ColorRes color: Int) {
            mBinding.listItemFeedUserTitle.text = context.getString(title)
            mBinding.listItemFeedUserTitle.setTextColor(ContextCompat.getColor(context, color))
        }

        private fun hideLikeCounter() {
            applyAnimation()
            mBinding.listItemLikesCounter.text = "0"
        }

        private fun setupLikesCounter(count: String) {
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
            val set = TransitionSet().addTransition(Scale(0.7f)).addTransition(Fade())
                .setInterpolator(FastOutLinearInInterpolator())
            TransitionManager.beginDelayedTransition(mBinding.listItemFeedPostParent, set)
        }


        fun onCommentButtonClick(view: View) {
            val post = mPostsFeed[adapterPosition]
            listener.onCommentButtonClick(post)
        }

        fun onLikeButtonClick(view: View) {
            showLikedButton()
            val post = mPostsFeed[adapterPosition]
            if (post.likes != null) {
                setupLikesCounter(numberCalculation(post.likes!!.size.plus(1)))
            } else {
                setupLikesCounter("1")
            }
            val like =
                Like(mFirebaseAuth.currentUser?.uid, post.registerToken, mRegisterToken, mFirebaseAuth.currentUser?.displayName, post.documentName, mFirebaseAuth.currentUser?.photoUrl.toString())
            listener.onLikeButtonClick(like, post.countryCode!!)
            likedArray.add(post.documentName!!)
        }

        private fun numberCalculation(number: Int): String {
            if (number < 1000) return "" + number
            val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
            return String.format("%.1f %c", number / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
        }

        fun onUnLikeButtonClick(view: View) {
            hideLikedButton()
            val post = mPostsFeed[adapterPosition]
            if (post.likes != null) {
                setupLikesCounter(numberCalculation(mBinding.listItemLikesCounter.text.toString().toInt().minus(1)))
            } else {
                hideLikeCounter()
            }
            val like =
                Like(mFirebaseAuth.currentUser?.uid, post.registerToken, mRegisterToken, mFirebaseAuth.currentUser?.displayName, post.documentName, mFirebaseAuth.currentUser?.photoUrl.toString())
            listener.onUnLikeButtonClick(like, post.countryCode!!)
            likedArray.remove(post.documentName!!)
        }

        fun onSharePostClick(view: View) {
            val post = mPostsFeed[adapterPosition]

            ShareCompat.IntentBuilder.from(context as AppCompatActivity).setType("text/plain")
                .setText("Checkout what ${post.authorName} posted on Social Note \n\n" + "${getPost(post.post!!)}")
                .setChooserTitle(context.getString(R.string.share_title)).startChooser()
        }

        @Suppress("DEPRECATION")
        private fun getPost(body: String?): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                HtmlCompat.fromHtml("$body ...", HtmlCompat.FROM_HTML_MODE_COMPACT)
                //or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE or HtmlCompat.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(body)
            }
        }

        fun onLikesCounterClick(view: View) {
            val post = mPostsFeed[adapterPosition]
            val intent = Intent(context, LikesActivity::class.java)
            intent.putExtra(Constants.USER_LIKES_INTENT_KEY, post.documentName)
            intent.putExtra(Constants.USER_COUNTRY_ISO_KEY, post.countryCode)
            context.startActivity(intent)
        }

        fun onUserImageClick(view: View) {
            val userUid = mPostsFeed[adapterPosition].authorUID!!
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(Constants.USER_UID_INTENT_KEY, userUid)
            context.startActivity(intent)
        }

        fun onPostLongClick(view: View): Boolean {
            listener.onPostLongClickListener(mPostsFeed[adapterPosition])
            return true
        }
    }
}


