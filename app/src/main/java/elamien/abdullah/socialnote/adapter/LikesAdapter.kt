package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.databinding.ListItemUserLikesBinding
import elamien.abdullah.socialnote.ui.ProfileActivity
import elamien.abdullah.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import elamien.abdullah.socialnote.utils.Constants.Companion.READER_TITLE
import elamien.abdullah.socialnote.utils.Constants.Companion.USER_UID_INTENT_KEY

/**
 * Created by AbdullahAtta on 06-Sep-19.
 */
class LikesAdapter(private val context: Context, private val mLikes: List<Like>) :
    RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemUserLikesBinding.inflate(inflater, parent, false)
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        holder.bind(mLikes[position])
    }

    override fun getItemCount(): Int = mLikes.size


    inner class LikesViewHolder(private val mBinding: ListItemUserLikesBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.handlers = this
        }

        fun bind(like: Like) {
            mBinding.like = like
            setUserTitle(like)
        }

        private fun setUserTitle(like: Like) {
            when (like.userTitle) {
                AUTHOR_TITLE -> showAuthorTitle()
                READER_TITLE -> showReaderTitle()
            }
        }

        private fun showReaderTitle() {
            mBinding.listItemUserAuthorTitle.visibility = View.GONE
            mBinding.listItemUserReaderTitle.visibility = View.VISIBLE
        }

        private fun showAuthorTitle() {
            mBinding.listItemUserAuthorTitle.visibility = View.VISIBLE
            mBinding.listItemUserReaderTitle.visibility = View.GONE
        }

        fun onLikeUserImageClick(view: View) {
            val userLikeUid = mLikes[adapterPosition].userLikerUId!!
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(USER_UID_INTENT_KEY, userLikeUid)
            context.startActivity(intent)
        }
    }
}