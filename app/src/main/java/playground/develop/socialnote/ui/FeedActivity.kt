package playground.develop.socialnote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.Observer
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.transitionseverywhere.extra.Scale
import org.jetbrains.anko.intentFor
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import playground.develop.socialnote.R
import playground.develop.socialnote.adapter.PostsFeedAdapter
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.databinding.ActivityFeedBinding
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.PreferenceUtils
import playground.develop.socialnote.viewmodel.PostViewModel


class FeedActivity : AppCompatActivity(), PostsFeedAdapter.PostInteractListener {

    private val mPostViewModel: PostViewModel by viewModel()
    private val mFirebaseAuth: FirebaseAuth by inject()
    private lateinit var mUser: User


    private lateinit var mBinding: ActivityFeedBinding
    private lateinit var mAdapter: PostsFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@FeedActivity, R.layout.activity_feed)
        mBinding.handlers = this

        mAdapter = PostsFeedAdapter(this@FeedActivity, this@FeedActivity, ArrayList<Post>())
        loadUser()
        loadPosts(mUserCountryCode!!)
        mBinding.userImageView.load(mFirebaseAuth.currentUser?.photoUrl) {
            transformations(CircleCropTransformation())
        }
        setupSwipeToRefresh()
    }

    private fun setupSwipeToRefresh() {
        mBinding.swipeRefresh.setColorSchemeResources(R.color.swipe_accent,
                                                      R.color.swipe_accent2,
                                                      R.color.swipe_primary)
        mBinding.swipeRefresh.setOnRefreshListener {
            loadPosts(mUserCountryCode!!)
        }
    }

    private val mUserCountryCode: String?
        get() {
            return PreferenceUtils.getPreferenceUtils()
                    .getUserCountryCode(this)
        }

    private fun loadUser() {
        mPostViewModel.getUser()
                .observe(this@FeedActivity, Observer { user ->
                    mUser = user
                })
    }

    private fun loadPosts(countryCode: String) {
        showLoadingView()
        mPostViewModel.getPosts(countryCode)
                .observe(this@FeedActivity, Observer { posts ->
                    if (posts.isNotEmpty()) {
                        mAdapter.addPosts(posts)
                        mBinding.feedRecyclerView.adapter = mAdapter
                        hideLoadingView()
                        mBinding.swipeRefresh.isRefreshing = false
                    }
                })
    }

    private fun showLoadingView() {
        applyAnimation()
        mBinding.feedRecyclerView.visibility = View.GONE
        mBinding.loadingAnimationView.visibility = View.VISIBLE
    }

    private fun applyAnimation() {
        val set = TransitionSet().addTransition(Scale(0.7f))
                .addTransition(Fade())
                .setInterpolator(FastOutLinearInInterpolator())
        TransitionManager.beginDelayedTransition(mBinding.postsFeedParent, set)
    }

    fun onPostClick(view: View) {
        startActivity(intentFor<CreatePostActivity>())
    }

    private fun hideLoadingView() {
        applyAnimation()
        mBinding.loadingAnimationView.visibility = View.GONE
        mBinding.feedRecyclerView.visibility = View.VISIBLE
    }

    override fun onCommentButtonClick(post: Post) {
        startActivity(intentFor<CommentActivity>(Constants.FIRESTORE_POST_DOC_INTENT_KEY to post.documentName,
                                                 Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY to post.registerToken,
                                                 Constants.USER_COUNTRY_ISO_KEY to post.countryCode))
    }

    fun onUserImageClick(view: View) {
        val userUid = mUser.userUid
        startActivity(intentFor<ProfileActivity>(Constants.USER_UID_INTENT_KEY to userUid))
    }

    override fun onLikeButtonClick(like: Like, postCountryCode: String) {
        like.userTitle = mUser.userTitle!!
        like.userImage = mUser.userImage
        mPostViewModel.createLikeOnPost(like, postCountryCode)
    }

    override fun onUnLikeButtonClick(like: Like, postCountryCode: String) {
        like.userTitle = mUser.userTitle!!
        like.userImage = mUser.userImage
        mPostViewModel.removeLikePost(like, postCountryCode)
    }

    override fun onPostLongClickListener(post: Post) {
        if (mUser.userUid == post.authorUID) {
            MaterialAlertDialogBuilder(this@FeedActivity).setTitle(getString(R.string.delete_post_dialog_title))
                    .setMessage(getString(R.string.delete_post_dialog_message))
                    .setNegativeButton(getString(R.string.delete_post_dialog_negative_button)) { dialog, id ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.delete_post_dialog_positive_button)) { dialog, id ->
                        mPostViewModel.deletePost(post)
                        loadPosts(mUserCountryCode!!)
                        dialog.dismiss()
                    }
                    .show()
        }
    }
}
