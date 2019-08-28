package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import elamien.abdullah.socialnote.databinding.ListItemCommentLeftBinding
import elamien.abdullah.socialnote.databinding.ListItemCommentRightBinding
import elamien.abdullah.socialnote.models.Comment
import java.util.*

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class CommentsAdapter(private val context : Context, private var mComments : List<Comment>) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val images = arrayOf("http://bit.ly/2PhvwfN",
			"http://bit.ly/2HpJ2aH",
			"http://bit.ly/327HLNz",
			"http://bit.ly/2Pd352y",
			"http://bit.ly/2MFbiKO",
			"http://bit.ly/341m7wh",
			"http://bit.ly/2U6i0dL",
			"http://bit.ly/2KZJ5fy")

	override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : RecyclerView.ViewHolder {
		when (viewType) {
			RIGHT_TYPE -> {
				val inflater = LayoutInflater.from(context)
				val binding = ListItemCommentRightBinding.inflate(inflater, parent, false)
				return RightCommentsViewHolder(binding)
			}
			LEFT_TYPE -> {
				val inflater = LayoutInflater.from(context)
				val binding = ListItemCommentLeftBinding.inflate(inflater, parent, false)
				return LeftCommentsViewHolder(binding)
			}
			else -> {
				val inflater = LayoutInflater.from(context)
				val binding = ListItemCommentRightBinding.inflate(inflater, parent, false)
				return RightCommentsViewHolder(binding)
			}
		}
	}

	override fun getItemViewType(position : Int) : Int {
		return if (position % 2 == 0) {
			RIGHT_TYPE
		} else {
			LEFT_TYPE
		}
	}

	override fun getItemCount() : Int = mComments.size

	override fun onBindViewHolder(holder : RecyclerView.ViewHolder, position : Int) {
		val comment = mComments[position]
		if (comment.comment!! == "") return

		when (holder.itemViewType) {
			RIGHT_TYPE -> {
				holder as RightCommentsViewHolder
				holder.bindRightComment(mComments[position])
			}
			LEFT_TYPE -> {
				holder as LeftCommentsViewHolder
				holder.bindLeftComment(mComments[position])
			}
			else -> {
				holder as RightCommentsViewHolder
				holder.bindRightComment(mComments[position])
			}
		}
	}

	inner class RightCommentsViewHolder(private val mBinding : ListItemCommentRightBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		fun bindRightComment(comment : Comment) {
			mBinding.comment = comment
			mBinding.listItemCommentDate.text =
				DateUtils.getRelativeTimeSpanString(comment.getDateCreated().time)
			mBinding.listItemCommentRightImage.load(getRandomImage()) {
				crossfade(true)
			}

		}
	}

	inner class LeftCommentsViewHolder(private val mBinding : ListItemCommentLeftBinding) :
		RecyclerView.ViewHolder(mBinding.root) {

		fun bindLeftComment(comment : Comment) {
			mBinding.comment = comment
			mBinding.listItemCommentDate.text =
				DateUtils.getRelativeTimeSpanString(comment.getDateCreated().time)
			mBinding.listItemCommentLeftImage.load(getRandomImage()) {
				crossfade(true)
			}
		}
	}

	private fun getRandomImage() = images[Random().nextInt(images.size)]
	fun addComments(comments : List<Comment>?) {
		mComments = comments!!
		notifyItemInserted(mComments.lastIndex)
	}

	companion object {
		const val RIGHT_TYPE = 1
		const val LEFT_TYPE = 2
	}
}