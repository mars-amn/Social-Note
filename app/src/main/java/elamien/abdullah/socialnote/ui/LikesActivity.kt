package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.LikesAdapter
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.databinding.ActivityLikesBinding
import elamien.abdullah.socialnote.utils.Constants.Companion.USER_LIKES_INTENT_KEY
import java.util.*


class LikesActivity : AppCompatActivity() {

	private lateinit var mBinding : ActivityLikesBinding
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this@LikesActivity, R.layout.activity_likes)
		if (intent != null && intent.hasExtra(USER_LIKES_INTENT_KEY)) {
			loadUserLikes(intent.getParcelableArrayListExtra<Like>(USER_LIKES_INTENT_KEY))
		} else {
			finish()
		}
	}

	private fun loadUserLikes(likes : ArrayList<Like>?) {
		if (likes != null) {
			val adapter = LikesAdapter(this@LikesActivity, likes)
			mBinding.userLikesRecyclerView.adapter = adapter
		} else {
			Toast.makeText(this@LikesActivity, "No one liked the post :( ", Toast.LENGTH_SHORT)
					.show()
		}
	}
}
