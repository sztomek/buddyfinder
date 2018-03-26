package hu.sztomek.wheresmybuddy.presentation.common.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import hu.sztomek.wheresmybuddy.R

class ErrorRecyclerRowViewHolder(parent: ViewGroup?) : RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_error, parent, false)) {

    @BindView(R.id.ivError)
    lateinit var ivError: ImageView
    @BindView(R.id.tvError)
    lateinit var tvError: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    fun bind(model: ErrorRecyclerRowModel) {
        model.customDrawable?.let {
            ivError.setImageDrawable(it)
        }
        model.customErrorMessage?.let {
            tvError.text = it
        }
    }
}