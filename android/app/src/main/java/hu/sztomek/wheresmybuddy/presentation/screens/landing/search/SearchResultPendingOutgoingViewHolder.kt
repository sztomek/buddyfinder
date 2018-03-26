package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchResultPendingOutgoingViewHolder(parent: ViewGroup?, imageLoader: ImageLoader) : SearchResultCommonViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_search_pending_outgoing, parent, false), imageLoader) {

    interface SearchResultItemPendingOutgoingClickListener : SearchResultCommonClickListener {
        fun onCancelClicked(requestId: String)
    }

    constructor(parent: ViewGroup?, searchResultItemClickListener: SearchResultItemPendingOutgoingClickListener, imageLoader: ImageLoader) : this(parent, imageLoader) {
        this.searchResultItemClickListener = searchResultItemClickListener
    }

    init {
        ButterKnife.bind(this, itemView)
    }

    @BindView(R.id.ivCancel)
    lateinit var ivCancel: ImageView

    var searchResultItemClickListener: SearchResultItemPendingOutgoingClickListener? = null
    override var detailsListener: SearchResultCommonClickListener? = null
        get() = searchResultItemClickListener


    fun bind(item: SearchResultItemModel) {
        bindProfile(item.profileModel)
        ivCancel.setOnClickListener({ _ ->
            searchResultItemClickListener?.onCancelClicked((item as SearchResultItemModel.PendingOutgoingItemModel).connectionRequestModel.id!!)
        })
    }

}