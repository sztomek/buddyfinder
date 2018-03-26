package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

abstract class SearchResultCommonViewHolder(view: View, private val imageLoader: ImageLoader) : RecyclerView.ViewHolder(view) {

    interface SearchResultCommonClickListener {
        fun onDetailsClicked(userId: String)
    }

    @BindView(R.id.tvName)
    lateinit var tvName: TextView
    @BindView(R.id.ivProfile)
    lateinit var ivPhoto: ImageView

    protected abstract var detailsListener: SearchResultCommonClickListener?

    init {
        ButterKnife.bind(this, view)
    }

    protected fun bindProfile(profileModel: ProfileModel) {
        tvName.text = profileModel.displayName
        imageLoader.loadImageInto(profileModel.profilePicture!!, ivPhoto)
        itemView.setOnClickListener { _ -> detailsListener?.onDetailsClicked(profileModel.id!!)}
    }

}