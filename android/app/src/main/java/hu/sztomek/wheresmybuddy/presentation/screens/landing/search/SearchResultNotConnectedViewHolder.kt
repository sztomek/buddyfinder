package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchResultNotConnectedViewHolder(parent: ViewGroup?, imageLoader: ImageLoader) : SearchResultCommonViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_search_not_connected, parent, false), imageLoader) {

    interface SearchResultItemClickListener : SearchResultCommonClickListener {
        fun onAddClicked(userId: String)
    }

    constructor(parent: ViewGroup?, searchResultItemClickListener: SearchResultItemClickListener, imageLoader: ImageLoader) : this(parent, imageLoader) {
        this.searchResultItemClickListener = searchResultItemClickListener
    }

    init {
        ButterKnife.bind(this, itemView)
    }

    @BindView(R.id.ivAdd)
    lateinit var ivAdd: ImageView

    var searchResultItemClickListener: SearchResultItemClickListener? = null
    override var detailsListener: SearchResultCommonClickListener? = null
        get() = searchResultItemClickListener


    fun bind(item: SearchResultItemModel) {
        bindProfile(item.profileModel)
        ivAdd.setOnClickListener({ _ ->
            searchResultItemClickListener?.onAddClicked(item.profileModel.id!!)
        })
    }

}