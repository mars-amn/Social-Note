package playground.develop.socialnote.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject
import playground.develop.socialnote.R
import playground.develop.socialnote.adapter.LikesAdapter
import playground.develop.socialnote.databinding.ActivityLikesBinding
import playground.develop.socialnote.utils.Constants.Companion.USER_COUNTRY_ISO_KEY
import playground.develop.socialnote.utils.Constants.Companion.USER_LIKES_INTENT_KEY
import playground.develop.socialnote.viewmodel.PostViewModel


class LikesActivity : AppCompatActivity() {
    private val mPostViewModel: PostViewModel by inject()
    private lateinit var mBinding: ActivityLikesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@LikesActivity, R.layout.activity_likes)
        if (intent != null && intent.hasExtra(USER_LIKES_INTENT_KEY)) {

            loadUserLikes(intent.getStringExtra(USER_LIKES_INTENT_KEY), intent.getStringExtra(USER_COUNTRY_ISO_KEY))
        } else {
            finish()
        }
    }

    private fun loadUserLikes(documentName: String?, postCountryCode: String?) {
        mPostViewModel.loadPost(documentName, postCountryCode!!)
            .observe(this@LikesActivity, Observer { post ->
                if (post.likes != null) {
                    val adapter = LikesAdapter(this@LikesActivity, post?.likes!!)
                    mBinding.userLikesRecyclerView.adapter = adapter
                }
            })
    }
}
