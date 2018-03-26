package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchResultPendingIncomingViewHolder(parent: ViewGroup?, imageLoader: ImageLoader) : SearchResultCommonViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_search_pending_incoming, parent, false), imageLoader) {

    interface SearchResultItemPendingIncomingClickListener : SearchResultCommonClickListener {
        fun onAcceptClicked(requestId: String)
        fun onDeclineClicked(requestId: String)
    }

    constructor(parent: ViewGroup?, searchResultItemClickListener: SearchResultItemPendingIncomingClickListener, imageLoader: ImageLoader) : this(parent, imageLoader) {
        this.searchResultItemClickListener = searchResultItemClickListener
    }

    init {
        ButterKnife.bind(this, itemView)
    }

    @BindView(R.id.ivDecline)
    lateinit var ivDecline: ImageView
    @BindView(R.id.ivAccept)
    lateinit var ivAccept: ImageView

    var searchResultItemClickListener: SearchResultItemPendingIncomingClickListener? = null
    override var detailsListener: SearchResultCommonClickListener? = null
        get() = searchResultItemClickListener

    fun bind(item: SearchResultItemModel) {
        bindProfile(item.profileModel)
        val connectionRequestModel = (item as SearchResultItemModel.PendingIncomingItemModel).connectionRequestModel
        ivAccept.setOnClickListener({ _ ->
            searchResultItemClickListener?.onAcceptClicked(connectionRequestModel.id!!)
        })
        ivDecline.setOnClickListener({ _ ->
            searchResultItemClickListener?.onDeclineClicked(connectionRequestModel.id!!)
        })
    }


}