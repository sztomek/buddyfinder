package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.model.FriendItemModel
import java.text.DateFormat

class FriendsAdapter(private val dateFormat: DateFormat, private val imageLoader: ImageLoader) : BaseRecyclerViewAdapter() {

    companion object {
        const val ROW_TYPE_FRIEND = 1
    }

    interface FriendClickListener {
        fun onProfileClicked(userId: String)
        fun onLocateClicked(userId: String)
    }

    var clickListener: FriendClickListener? = null

    override fun getItemViewType(position: Int): Int {
        return when(data[position]) {
            is FriendItemModel -> ROW_TYPE_FRIEND
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
       return when(viewType) {
           ROW_TYPE_FRIEND -> FriendItemViewHolder(parent)
           else -> super.onCreateViewHolder(parent, viewType)
       }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is FriendItemViewHolder -> holder.bind(data[position] as FriendItemModel)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    inner class FriendItemViewHolder(parent: ViewGroup?) : RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_friend, parent, false)) {

        init {
            ButterKnife.bind(this, itemView)
        }

        @BindView(R.id.tvName)
        lateinit var tvName: TextView
        @BindView(R.id.ivProfile)
        lateinit var ivProfile: ImageView
        @BindView(R.id.vtvTrusted)
        lateinit var vtvTrusted: View
        @BindView(R.id.spinnerStatus)
        lateinit var tvStatus: TextView
        @BindView(R.id.tvDistance)
        lateinit var tvDistance: TextView
        @BindView(R.id.ivLocate)
        lateinit var vLocate: View

        fun bind(model: FriendItemModel) {
            tvName.text = model.profile.displayName
            imageLoader.loadImageInto(model.profile.profilePicture!!, ivProfile)
            vtvTrusted.visibility = if (model.connectionModel.trusted == true) View.VISIBLE else View.GONE
            tvStatus.text = model.statusInfo.getStatusMessage(itemView.resources, dateFormat)
            tvDistance.text = model.statusInfo.getDistanceMessage(itemView.resources)
            itemView.setOnClickListener {
                _ -> clickListener?.onProfileClicked(model.profile.id!!)
            }
            vLocate.setOnClickListener {
                _ -> clickListener?.onLocateClicked(model.profile.id!!)
            }
        }

    }
}