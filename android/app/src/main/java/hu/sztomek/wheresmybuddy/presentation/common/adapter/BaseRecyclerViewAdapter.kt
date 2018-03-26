package hu.sztomek.wheresmybuddy.presentation.common.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import timber.log.Timber

abstract class BaseRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ROW_TYPE_LOADING = 444
        const val ROW_TYPE_ERROR = 555
    }

    protected val data = mutableListOf<Any>()
    private val dataLock = Object()

    override fun getItemCount(): Int {
        return data.size
    }

    fun addData(newData: List<Any>) {
        synchronized(dataLock) {
            val mutableList = data.toMutableList()
            mutableList.addAll(newData)
            updateList(mutableList.toList())
        }
    }

    fun setData(newData: List<Any>) {
        synchronized(dataLock) {
            updateList(newData)
        }
    }

    fun getItems(): List<Any> {
        synchronized(dataLock) {
            return data
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is LoadingRecyclerRowModel -> ROW_TYPE_LOADING
            is ErrorRecyclerRowViewHolder -> ROW_TYPE_ERROR
            else -> {
                Timber.d("No view type for item: [${data[position]}]")
                return -1
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when(viewType) {
            ROW_TYPE_ERROR -> ErrorRecyclerRowViewHolder(parent)
            ROW_TYPE_LOADING -> LoadingRecyclerRowViewHolder(parent)
            else -> {
                Timber.d("Unknown viewType [$viewType]")
                throw IllegalArgumentException("Unknown viewType [$viewType]")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(holder) {
            is LoadingRecyclerRowViewHolder -> holder.bind(data[position] as LoadingRecyclerRowModel)
            is ErrorRecyclerRowViewHolder -> holder.bind(data[position] as ErrorRecyclerRowModel)
        }
    }

    private fun updateList(newData: List<Any>) {
        val diffResult = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] === newData[newItemPosition]
            }

            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return newData.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == newData[newItemPosition]
            }
        })
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

}