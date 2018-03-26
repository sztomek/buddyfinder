package hu.sztomek.wheresmybuddy.presentation.common.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R

class LoadingRecyclerRowViewHolder(parent: ViewGroup?) : RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_loading, parent, false)) {

    @BindView(R.id.tvLoading)
    lateinit var tvLoading: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(model: LoadingRecyclerRowModel) {
        model.customLoadingMessage?.let {
            tvLoading.text = it
        }
    }
}