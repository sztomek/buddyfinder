package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel

class SearchResultLoadingViewHolder(parent: ViewGroup?, imageLoader: ImageLoader) : SearchResultCommonViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_search_loading, parent, false), imageLoader) {

    constructor(parent: ViewGroup?, listener: SearchResultCommonClickListener, imageLoader: ImageLoader) : this(parent, imageLoader) {
        _detailsListener = listener
    }

    @BindView(R.id.pbLoading)
    lateinit var pbLoading: ProgressBar

    private var _detailsListener: SearchResultCommonClickListener? = null
    override var detailsListener: SearchResultCommonClickListener? = null
        get() = _detailsListener

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(item: SearchResultItemModel.LoadingItemModel) {
        bindProfile(item.profileModel)
    }
}