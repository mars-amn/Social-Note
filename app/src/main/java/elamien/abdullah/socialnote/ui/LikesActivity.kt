package elamien.abdullah.socialnote.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.LikesAdapter
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.databinding.ActivityLikesBinding
import elamien.abdullah.socialnote.utils.Constants.Companion.USER_LIKES_INTENT_KEY
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject


class LikesActivity : AppCompatActivity() {
    private val mPostViewModel: PostViewModel by inject()
    private lateinit var mBinding: ActivityLikesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@LikesActivity, R.layout.activity_likes)
        if (intent != null && intent.hasExtra(USER_LIKES_INTENT_KEY)) {
            loadUserLikes(intent.getStringExtra(USER_LIKES_INTENT_KEY))
        } else {
            finish()
        }
    }

    private fun loadUserLikes(documentName: String?) {
        mPostViewModel.loadPost(documentName)
            .observe(this@LikesActivity, Observer { post ->
                val adapter = LikesAdapter(this@LikesActivity, post?.likes as List<Like>)
                mBinding.userLikesRecyclerView.adapter = adapter
            })
    }
}
