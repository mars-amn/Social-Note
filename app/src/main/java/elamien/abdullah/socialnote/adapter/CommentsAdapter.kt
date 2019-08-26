package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.databinding.ListItemCommentBinding
import elamien.abdullah.socialnote.models.Comment

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class CommentsAdapter(private val context : Context, private val comments : List<Comment>) :
	RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

	override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : CommentsViewHolder {
		val inflater = LayoutInflater.from(context)
		val binding = ListItemCommentBinding.inflate(inflater, parent, false)
		return CommentsViewHolder(binding)
	}

	override fun getItemCount() : Int = comments.size

	override fun onBindViewHolder(holder : CommentsViewHolder, position : Int) {
		holder.bind(comments[position])
	}

	inner class CommentsViewHolder(private val mBinding : ListItemCommentBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		fun bind(comment : Comment) {
			mBinding.comment = comment
			mBinding.listItemCommentDate.text =
				DateUtils.getRelativeTimeSpanString(comment.getDateCreated().time)
		}

	}
}