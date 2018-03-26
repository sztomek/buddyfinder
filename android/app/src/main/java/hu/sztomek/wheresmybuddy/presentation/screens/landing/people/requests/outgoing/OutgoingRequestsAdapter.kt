package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing

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
import hu.sztomek.wheresmybuddy.presentation.model.PendingRequestItemModel
import java.text.DateFormat

class OutgoingRequestsAdapter(private val dateFormat: DateFormat, private val imageLoader: ImageLoader) : BaseRecyclerViewAdapter() {

    companion object {
        const val ROW_TYPE_OUTGOING = 33
    }

    interface OutgoingRequestClickListener {
        fun onProfileClicked(profileId: String)
        fun onCancelClicked(requestId: String)
    }

    var clickListener: OutgoingRequestClickListener? = null

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is PendingRequestItemModel -> ROW_TYPE_OUTGOING
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            ROW_TYPE_OUTGOING -> OutgoingRequestViewHolder(parent)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is OutgoingRequestViewHolder -> holder.bind(data[position] as PendingRequestItemModel)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    inner class OutgoingRequestViewHolder(parent: ViewGroup?) : RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_outgoing_request, parent, false)) {

        init {
            ButterKnife.bind(this, itemView)
        }

        @BindView(R.id.tvName)
        lateinit var tvName: TextView
        @BindView(R.id.ivProfile)
        lateinit var ivProfile: ImageView
        @BindView(R.id.tvInfo)
        lateinit var tvInfo: TextView
        @BindView(R.id.btnCancel)
        lateinit var vCancel: View

        fun bind(model: PendingRequestItemModel) {
            tvName.text = model.profile.displayName
            imageLoader.loadImageInto(model.profile.profilePicture!!, ivProfile)
            tvInfo.text = model.infoModel.getSentText(itemView.resources, dateFormat)
            itemView.setOnClickListener { _ ->
                clickListener?.onProfileClicked(model.profile.id!!)
            }
            vCancel.setOnClickListener { _ ->
                clickListener?.onCancelClicked(model.connectionRequestModel.id!!)
            }
        }

    }
}