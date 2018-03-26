package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchResultConnectedViewHolder(parent: ViewGroup?, imageLoader: ImageLoader) : SearchResultCommonViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_search_connected, parent, false), imageLoader) {

    interface SearchResultItemConnectedClickListener : SearchResultCommonClickListener {
        fun onLocateClicked(userId: String)
    }

    constructor(parent: ViewGroup?, searchResultItemClickListener: SearchResultItemConnectedClickListener, imageLoader: ImageLoader) : this(parent, imageLoader) {
        this.searchResultItemClickListener = searchResultItemClickListener
    }

    init {
        ButterKnife.bind(this, itemView)
    }

    @BindView(R.id.ivLocate)
    lateinit var ivLocate: ImageView

    var searchResultItemClickListener: SearchResultItemConnectedClickListener? = null
    override var detailsListener: SearchResultCommonClickListener? = null
        get() = searchResultItemClickListener


    fun bind(item: SearchResultItemModel) {
        bindProfile(item.profileModel)
        ivLocate.setOnClickListener({ _ ->
            searchResultItemClickListener?.onLocateClicked(item.profileModel.id!!)
        })
    }

}