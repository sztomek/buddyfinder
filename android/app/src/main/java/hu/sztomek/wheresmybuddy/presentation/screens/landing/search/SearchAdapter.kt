package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchAdapter(var clickListener: SearchAdapterClickListener?, private val imageLoader: ImageLoader) : BaseRecyclerViewAdapter() {

    companion object {
        const val ITEM_NOT_CONNECTED = 1
        const val ITEM_OUTGOING_REQUEST = 2
        const val ITEM_INCOMING_REQUEST = 3
        const val ITEM_CONNECTED = 4
        const val ITEM_TRUSTED = 5
        const val ITEM_LOADING = 9
    }

    interface SearchAdapterClickListener {
        fun onDetailsClick(userId: String)
        fun onSendRequestClick(userId: String)
        fun onCancelRequestClick(requestId: String)
        fun onAcceptRequestClick(requestId: String)
        fun onDeclineRequestClick(requestId: String)
        fun onLocateClick(userId: String)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is SearchResultLoadingViewHolder -> holder.bind(data[position] as SearchResultItemModel.LoadingItemModel)
            is SearchResultPendingOutgoingViewHolder -> holder.bind(data[position] as SearchResultItemModel.PendingOutgoingItemModel)
            is SearchResultNotConnectedViewHolder -> holder.bind(data[position] as SearchResultItemModel.NotConnectedItemModel)
            is SearchResultPendingIncomingViewHolder -> holder.bind(data[position] as SearchResultItemModel.PendingIncomingItemModel)
            is SearchResultConnectedViewHolder -> holder.bind(data[position] as SearchResultItemModel.ConnectedItemModel)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is SearchResultItemModel.NotConnectedItemModel -> ITEM_NOT_CONNECTED
            is SearchResultItemModel.LoadingItemModel -> ITEM_LOADING
            is SearchResultItemModel.PendingOutgoingItemModel -> ITEM_OUTGOING_REQUEST
            is SearchResultItemModel.PendingIncomingItemModel -> ITEM_INCOMING_REQUEST
            is SearchResultItemModel.ConnectedItemModel, is SearchResultItemModel.TrustedItemModel -> ITEM_CONNECTED
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            ITEM_NOT_CONNECTED -> SearchResultNotConnectedViewHolder(parent, object : SearchResultNotConnectedViewHolder.SearchResultItemClickListener {
                override fun onAddClicked(userId: String) {
                    clickListener?.onSendRequestClick(userId)
                }

                override fun onDetailsClicked(userId: String) {
                    clickListener?.onDetailsClick(userId)
                }
            }, imageLoader)
            ITEM_LOADING -> SearchResultLoadingViewHolder(parent, object : SearchResultCommonViewHolder.SearchResultCommonClickListener {
                override fun onDetailsClicked(userId: String) {
                    clickListener?.onDetailsClick(userId)
                }
            }, imageLoader)
            ITEM_OUTGOING_REQUEST -> SearchResultPendingOutgoingViewHolder(parent, object : SearchResultPendingOutgoingViewHolder.SearchResultItemPendingOutgoingClickListener {
                override fun onCancelClicked(requestId: String) {
                    clickListener?.onCancelRequestClick(requestId)
                }

                override fun onDetailsClicked(userId: String) {
                    clickListener?.onDetailsClick(userId)
                }
            }, imageLoader)
            ITEM_INCOMING_REQUEST -> SearchResultPendingIncomingViewHolder(parent, object : SearchResultPendingIncomingViewHolder.SearchResultItemPendingIncomingClickListener {
                override fun onAcceptClicked(requestId: String) {
                    clickListener?.onAcceptRequestClick(requestId)
                }

                override fun onDeclineClicked(requestId: String) {
                    clickListener?.onDeclineRequestClick(requestId)
                }

                override fun onDetailsClicked(userId: String) {
                    clickListener?.onDetailsClick(userId)
                }
            }, imageLoader)
            ITEM_CONNECTED -> SearchResultConnectedViewHolder(parent, object : SearchResultConnectedViewHolder.SearchResultItemConnectedClickListener {
                override fun onLocateClicked(userId: String) {
                    clickListener?.onLocateClick(userId)
                }

                override fun onDetailsClicked(userId: String) {
                    clickListener?.onDetailsClick(userId)
                }
            }, imageLoader)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

}