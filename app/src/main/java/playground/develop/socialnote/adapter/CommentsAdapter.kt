package playground.develop.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import playground.develop.socialnote.database.remote.firestore.models.Comment
import playground.develop.socialnote.databinding.ListItemCommentLeftBinding
import playground.develop.socialnote.databinding.ListItemCommentRightBinding
import playground.develop.socialnote.ui.ProfileActivity
import playground.develop.socialnote.utils.Constants
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
class CommentsAdapter(private val context: Context, private val mCommentListener: CommentListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), AutoUpdatableAdapter {

    private val images = arrayOf("http://bit.ly/2PhvwfN",
                                 "http://bit.ly/2HpJ2aH",
                                 "http://bit.ly/327HLNz",
                                 "http://bit.ly/2Pd352y",
                                 "http://bit.ly/2MFbiKO",
                                 "http://bit.ly/341m7wh",
                                 "http://bit.ly/2U6i0dL",
                                 "http://bit.ly/2KZJ5fy")

    interface CommentListener {
        fun onCommentLongClick(comment: Comment)
    }

    var mComments: List<Comment> by Delegates.observable(emptyList()) { prop, old, new ->
        autoNotify(old, new) { o, n -> o.getDateCreated() == n.getDateCreated() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            RIGHT_TYPE
        } else {
            LEFT_TYPE
        }
    }

    override fun getItemCount(): Int = mComments.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

    inner class RightCommentsViewHolder(private val mBinding: ListItemCommentRightBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
        init {
            mBinding.handlers = this
        }

        fun bindRightComment(comment: Comment) {
            mBinding.comment = comment
            mBinding.listItemCommentDate.text = DateUtils.getRelativeTimeSpanString(comment.getDateCreated().time)
            mBinding.listItemCommentRightImage.load(getRandomImage()) {
                crossfade(true)
            }
            setUserTitle(comment.authorTitle!!)
        }

        private fun setUserTitle(title: String) {
            when (title) {
                Constants.READER_TITLE -> showReaderTitle()
                Constants.AUTHOR_TITLE -> showAuthorTitle()
            }
        }

        private fun showAuthorTitle() {
            mBinding.listItemFeedUserReaderTitle.visibility = View.GONE
            mBinding.listItemFeedUserAuthorTitle.visibility = View.VISIBLE
        }

        private fun showReaderTitle() {
            mBinding.listItemFeedUserReaderTitle.visibility = View.VISIBLE
            mBinding.listItemFeedUserAuthorTitle.visibility = View.GONE
        }

        fun onUserImageClick(view: View) {
            val userUid = mComments[adapterPosition].authorUId
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(Constants.USER_UID_INTENT_KEY, userUid)
            context.startActivity(intent)
        }

        fun onCommentLongClick(view: View): Boolean {
            mCommentListener.onCommentLongClick(mComments[adapterPosition])
            return true
        }
    }

    inner class LeftCommentsViewHolder(private val mBinding: ListItemCommentLeftBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
        init {
            mBinding.handlers = this
        }

        fun bindLeftComment(comment: Comment) {
            mBinding.comment = comment
            mBinding.listItemCommentDate.text = DateUtils.getRelativeTimeSpanString(comment.getDateCreated().time)
            mBinding.listItemCommentLeftImage.load(getRandomImage()) {
                crossfade(true)
            }
            setUserTitle(comment.authorTitle!!)
        }

        private fun setUserTitle(title: String) {
            when (title) {
                Constants.READER_TITLE -> showReaderTitle()
                Constants.AUTHOR_TITLE -> showAuthorTitle()
            }
        }

        private fun showAuthorTitle() {
            mBinding.listItemFeedUserReaderTitle.visibility = View.GONE
            mBinding.listItemFeedUserAuthorTitle.visibility = View.VISIBLE
        }

        private fun showReaderTitle() {
            mBinding.listItemFeedUserReaderTitle.visibility = View.VISIBLE
            mBinding.listItemFeedUserAuthorTitle.visibility = View.GONE
        }

        fun onUserImageClick(view: View) {
            val userUid = mComments[adapterPosition].authorUId
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(Constants.USER_UID_INTENT_KEY, userUid)
            context.startActivity(intent)
        }

        fun onCommentLongClick(view: View): Boolean {
            mCommentListener.onCommentLongClick(mComments[adapterPosition])
            return true
        }
    }

    private fun getRandomImage() = images[Random().nextInt(images.size)]

    companion object {
        const val RIGHT_TYPE = 1
        const val LEFT_TYPE = 2
    }
}