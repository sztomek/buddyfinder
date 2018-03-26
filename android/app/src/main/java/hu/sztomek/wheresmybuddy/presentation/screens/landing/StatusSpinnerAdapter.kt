package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.Helpers

class StatusSpinnerAdapter(private val items: List<StatusSpinnerItem>) : BaseAdapter() {

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        return if (view == null) {
            val viewHolder = StatusViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_broadcast_status, parent, false) as TextView)
            viewHolder.bind(items[position])
            viewHolder.textView.tag = viewHolder

            viewHolder.textView
        } else {
            Helpers.safeCastTo<StatusViewHolder>(view.tag)?.bind(items[position])

            view
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (convertView == null) {
            val viewHolder = StatusViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_broadcast_status_dropdown, parent, false) as TextView)
            viewHolder.bind(items[position])
            viewHolder.textView.tag = viewHolder

            viewHolder.textView
        } else {
            Helpers.safeCastTo<StatusViewHolder>(convertView.tag)?.bind(items[position])

            convertView
        }
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    class StatusViewHolder(val textView: TextView) {

        fun bind(item: StatusSpinnerItem) {
            textView.setText(item.labelRes)
            textView.setCompoundDrawablesWithIntrinsicBounds(item.drawableRes, 0, 0, 0)
        }

    }

}