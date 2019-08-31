package elamien.abdullah.socialnote.ui

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.transitionseverywhere.extra.Scale
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.PostsFeedAdapter
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.databinding.ActivityFeedBinding
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedActivity : AppCompatActivity(), PostsFeedAdapter.LikeClickListener {

	private val mPostViewModel : PostViewModel by viewModel()
	private val mFirebaseAuth : FirebaseAuth by inject()


	private lateinit var mBinding : ActivityFeedBinding
	private lateinit var mAdapter : PostsFeedAdapter
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this@FeedActivity, R.layout.activity_feed)
		mBinding.handlers = this
		mAdapter = PostsFeedAdapter(this@FeedActivity, this@FeedActivity, ArrayList<Post>())

		loadPosts()
		mBinding.userImageView.load(mFirebaseAuth.currentUser?.photoUrl) {
			transformations(CircleCropTransformation())
		}
	}

	private fun loadPosts() {
		showLoadingView()
		mPostViewModel.getPosts()
				.observe(this, Observer { posts ->
					if (posts.isNotEmpty()) {
						mAdapter.addPosts(posts)
						mBinding.feedRecyclerView.adapter = mAdapter
						hideLoadingView()
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

	fun onPostClick(view : View) {
		startActivity(Intent(this@FeedActivity, CreatePostActivity::class.java))
	}

	fun hideLoadingView() {
		applyAnimation()
		mBinding.loadingAnimationView.visibility = View.GONE
		mBinding.feedRecyclerView.visibility = View.VISIBLE
	}

	override fun onLikeButtonClick(like : Like) {
		mPostViewModel.createLikeOnPost(like)
	}

	override fun onUnLikeButtonClick(like : Like) {
		mPostViewModel.deleteRemoveLikeOfPost(like)
	}
}
