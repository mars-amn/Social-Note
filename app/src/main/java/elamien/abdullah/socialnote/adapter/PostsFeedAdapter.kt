package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.databinding.ListItemFeedBinding
import elamien.abdullah.socialnote.models.Post
import elamien.abdullah.socialnote.ui.CommentActivity
import elamien.abdullah.socialnote.utils.Constants

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class PostsFeedAdapter(private val context : Context, private val mPostsFeed : List<Post>) :
	RecyclerView.Adapter<PostsFeedAdapter.PostsFeedViewHolder>() {


	override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PostsFeedViewHolder {
		val inflater = LayoutInflater.from(context)
		val binding = ListItemFeedBinding.inflate(inflater, parent, false)
		return PostsFeedViewHolder(binding)
	}

	override fun onBindViewHolder(holder : PostsFeedViewHolder, position : Int) {
		holder.bind(mPostsFeed[position])
	}

	override fun getItemCount() : Int = mPostsFeed.size


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
					mPostsFeed[adapterPosition].documentName)
			intent.putExtra(Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY,
					mPostsFeed[adapterPosition].registerToken)
			context.startActivity(intent)
		}

	}
}