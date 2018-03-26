package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming

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

class IncomingRequestsAdapter(private val dateFormat: DateFormat, private val imageLoader: ImageLoader) : BaseRecyclerViewAdapter() {

    companion object {
        const val ROW_TYPE_INCOMING = 11
    }

    interface IncomingRequestClickListener {
        fun onProfileClicked(userId: String)
        fun onAcceptClicked(requestId: String)
        fun onDeclineClicked(requestId: String)
    }

    var clickListener: IncomingRequestClickListener? = null

    override fun getItemViewType(position: Int): Int {
        return when(data[position]) {
            is PendingRequestItemModel -> ROW_TYPE_INCOMING
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            ROW_TYPE_INCOMING -> IncomingRequestViewHolder(parent)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is IncomingRequestViewHolder -> holder.bind(data[position] as PendingRequestItemModel)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    inner class IncomingRequestViewHolder(parent: ViewGroup?) : RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_incoming_request, parent, false)) {

        init {
            ButterKnife.bind(this, itemView)
        }

        @BindView(R.id.tvName)
        lateinit var tvName: TextView
        @BindView(R.id.ivProfile)
        lateinit var ivProfile: ImageView
        @BindView(R.id.tvInfo)
        lateinit var tvInfo: TextView
        @BindView(R.id.btnAccept)
        lateinit var vAccept: View
        @BindView(R.id.btnCancel)
        lateinit var vDecline: View

        fun bind(model: PendingRequestItemModel) {
            tvName.text = model.profile.displayName
            imageLoader.loadImageInto(model.profile.profilePicture!!, ivProfile)
            tvInfo.text = model.infoModel.getSentText(itemView.resources, dateFormat)
            itemView.setOnClickListener {
                _ -> clickListener?.onProfileClicked(model.profile.id!!)
            }
            vAccept.setOnClickListener {
                _ -> clickListener?.onAcceptClicked(model.connectionRequestModel.id!!)
            }
            vDecline.setOnClickListener {
                _ -> clickListener?.onDeclineClicked(model.connectionRequestModel.id!!)
            }

        }

    }
}