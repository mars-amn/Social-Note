package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import coil.api.load
import com.google.firebase.auth.FirebaseAuth
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.PostsFeedAdapter
import elamien.abdullah.socialnote.databinding.ActivityFeedBinding
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedActivity : AppCompatActivity() {

	private val mPostViewModel : PostViewModel by viewModel()
	private val mFirebaseAuth : FirebaseAuth by inject()

	private lateinit var mBinding : ActivityFeedBinding
	private lateinit var mAdapter : PostsFeedAdapter
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this@FeedActivity, R.layout.activity_feed)
		mBinding.handlers = this
		initFeedAdapter()
		loadPosts()
		mBinding.userImageView.load(mFirebaseAuth.currentUser?.photoUrl)
	}

	private fun initFeedAdapter() {
		mAdapter = PostsFeedAdapter(this@FeedActivity, ArrayList())
		mBinding.feedRecyclerView.adapter = mAdapter
	}

	private fun loadPosts() {
		mPostViewModel.getPosts()
				.observe(this, Observer { posts ->
					mAdapter.addPosts(posts)

				})
	}

	fun onPostClick(view : View) {
		startActivity(Intent(this@FeedActivity, CreatePostActivity::class.java))
	}
}
