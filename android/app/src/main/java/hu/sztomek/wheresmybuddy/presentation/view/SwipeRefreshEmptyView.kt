package hu.sztomek.wheresmybuddy.presentation.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import hu.sztomek.wheresmybuddy.R

class SwipeRefreshEmptyView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var emptyMessage: String
    var showEmptyView: Boolean = true
        set(value) {
            field = value
            refresh()
        }

    private val tvEmpty: TextView
    private val srlContainer: SwipeRefreshLayout

    init {

        val typedArray = context.theme.obtainStyledAttributes(
                attrs
                , R.styleable.SwipeRefreshEmptyView
                , defStyleAttr
                , 0
        )

        tvEmpty = LayoutInflater.from(context).inflate(R.layout.layout_swipe_refresh_empty, this).findViewById(R.id.tvEmptyMessage)
        srlContainer = findViewById(R.id.srlRefresh)

        try {
            emptyMessage = if (typedArray.hasValue(R.styleable.SwipeRefreshEmptyView_emptyMessage)) typedArray.getString(R.styleable.SwipeRefreshEmptyView_emptyMessage)
            else context.getString(R.string.default_empty_message)
        } finally {
            typedArray.recycle()
        }

        showEmptyView = true

    }

    private fun refresh() {
        tvEmpty.visibility = if (showEmptyView) View.VISIBLE else View.GONE
        srlContainer.visibility = if (showEmptyView) View.GONE else View.VISIBLE
        tvEmpty.text = emptyMessage
    }

    fun getSwipeRefreshLayout(): SwipeRefreshLayout {
        return srlContainer
    }

}