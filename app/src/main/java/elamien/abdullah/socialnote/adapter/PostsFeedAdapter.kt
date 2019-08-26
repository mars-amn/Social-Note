package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import elamien.abdullah.socialnote.databinding.ListItemFeedBinding
import elamien.abdullah.socialnote.models.Post
import elamien.abdullah.socialnote.ui.CommentActivity
import elamien.abdullah.socialnote.utils.Constants

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostsFeedAdapter(private val context : Context, options : FirestoreRecyclerOptions<Post>) :
	FirestoreRecyclerAdapter<Post, PostsFeedAdapter.PostsFeedViewHolder>(options) {

	override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PostsFeedViewHolder {
		val inflater = LayoutInflater.from(context)
		val binding = ListItemFeedBinding.inflate(inflater, parent, false)
		return PostsFeedViewHolder(binding)
	}

	override fun onBindViewHolder(holder : PostsFeedViewHolder, position : Int, post : Post) {
		holder.bind(getItem(position))
	}

	inner class PostsFeedViewHolder(private val mBinding : ListItemFeedBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		init {
			mBinding.handlers = this
		}

		fun bind(post : Post) {
			mBinding.post = post
		}

		fun onUpvoteButtonClick(view : View) {
			mBinding.listItemFeedUpvoteButton.playAnimation()
		}

		fun onCommentButtonClick(view : View) {
			val intent = Intent(context, CommentActivity::class.java)
			intent.putExtra(Constants.FIRESTORE_POST_DOC_INTENT_KEY,
					getItem(adapterPosition).documentName)
			context.startActivity(intent)
		}

	}
}