package elamien.abdullah.socialnote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import elamien.abdullah.socialnote.database.remote.firestore.models.Like
import elamien.abdullah.socialnote.databinding.ListItemUserLikesBinding

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
        }

        fun onLikeUserImageClick(view: View) {
            //open user profile
        }
    }
}