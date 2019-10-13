package playground.develop.socialnote.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import playground.develop.socialnote.R
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.databinding.ListItemUserLikesBinding
import playground.develop.socialnote.ui.ProfileActivity
import playground.develop.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.ORIGINATOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.READER_TITLE
import playground.develop.socialnote.utils.Constants.Companion.USER_UID_INTENT_KEY

/**
 * Created by AbdullahAtta on 06-Sep-19.
 */
class LikesAdapter(private val context: Context, private val mLikes: List<Like>) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemUserLikesBinding.inflate(inflater, parent, false)
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        holder.bind(mLikes[position])
    }

    override fun getItemCount(): Int = mLikes.size


    inner class LikesViewHolder(private val mBinding: ListItemUserLikesBinding) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.handlers = this
        }

        fun bind(like: Like) {
            mBinding.like = like
            setUserTitle(like.userTitle!!)
        }

        private fun setUserTitle(title: String) {
            when (title) {
                READER_TITLE -> setTitle(R.string.reader_title, R.color.reader_title_color)
                AUTHOR_TITLE -> setTitle(R.string.author_title, R.color.author_title_color)
                ORIGINATOR_TITLE -> setTitle(R.string.originator_title, R.color.originator_title_color)
            }
        }

        private fun setTitle(@StringRes title: Int, @ColorRes color: Int) {
            mBinding.listItemUserTitle.text = context.getString(title)
            mBinding.listItemUserTitle.setTextColor(ContextCompat.getColor(context, color))
        }

        fun onLikeUserImageClick(view: View) {
            val userLikeUid = mLikes[adapterPosition].userLikerUId!!
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(USER_UID_INTENT_KEY, userLikeUid)
            context.startActivity(intent)
        }
    }
}