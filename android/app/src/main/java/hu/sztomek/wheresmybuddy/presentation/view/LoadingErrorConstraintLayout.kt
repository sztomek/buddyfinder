package hu.sztomek.wheresmybuddy.presentation.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import hu.sztomek.wheresmybuddy.R

class LoadingErrorConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface RetryListener {
        fun onRetryClick()
    }

    companion object {
        const val STATE_IDLE = 1
        const val STATE_LOADING = 2
        const val STATE_ERROR = 3
    }

    var retryListener: RetryListener? = null
    var loadingMessage: String?
    var errorMessage: String?
    var retryImage: Drawable?
    var state = STATE_IDLE
        set(value) {
            field = value
            refresh()
        }

    private val tvLoadingMessage: TextView
    private val tvErrorMessage: TextView
    private val ivImage: ImageView
    private val loading: View
    private val error: View
    var content: View?  = null

    init {

        loading = LayoutInflater.from(context).inflate(R.layout.layout_loading, this).findViewById(R.id.cvLoading)
        error = LayoutInflater.from(context).inflate(R.layout.layout_error, this).findViewById(R.id.clError)
        error.setOnClickListener({ retryListener?.onRetryClick() })

        tvLoadingMessage = loading.findViewById(R.id.tvLoadingMessage)
        tvErrorMessage = error.findViewById(R.id.tvErrorMessage)
        ivImage = error.findViewById(R.id.ivImage)

        val typedArray = context.theme.obtainStyledAttributes(
                attrs
                , R.styleable.LoadingErrorConstraintLayout
                , defStyleAttr
                , 0
        )

        try {
            loadingMessage = if (typedArray.hasValue(R.styleable.LoadingErrorConstraintLayout_loadingMessage)) typedArray.getString(R.styleable.LoadingErrorConstraintLayout_loadingMessage)
            else context.getString(R.string.default_loading_message)
            errorMessage = if (typedArray.hasValue(R.styleable.LoadingErrorConstraintLayout_retryMessage)) typedArray.getString(R.styleable.LoadingErrorConstraintLayout_retryMessage)
            else context.getString(R.string.default_error_message)
            retryImage = if (typedArray.hasValue(R.styleable.LoadingErrorConstraintLayout_retryImage)) typedArray.getDrawable(R.styleable.LoadingErrorConstraintLayout_retryImage)
            else ContextCompat.getDrawable(context, R.drawable.ic_error)
            state = if (typedArray.hasValue(R.styleable.LoadingErrorConstraintLayout_state)) typedArray.getInt(R.styleable.LoadingErrorConstraintLayout_state, STATE_IDLE)
            else STATE_IDLE
            if (typedArray.hasValue(R.styleable.LoadingErrorConstraintLayout_content)) {
                content = LayoutInflater.from(context).inflate(typedArray.getResourceId(R.styleable.LoadingErrorConstraintLayout_content, 0), this)
            }
        } finally {
            typedArray.recycle()
        }

    }

    private fun refresh() {
        when (state) {
            STATE_IDLE -> {
                loading.visibility = View.GONE
                error.visibility = View.GONE
                content?.visibility = View.VISIBLE
            }
            STATE_ERROR -> {
                loading.visibility = View.GONE
                error.visibility = View.VISIBLE
                tvErrorMessage.text = errorMessage
                ivImage.setImageDrawable(retryImage)
                content?.visibility = View.GONE
            }
            STATE_LOADING -> {
                loading.visibility = View.VISIBLE
                error.visibility = View.GONE
                tvLoadingMessage.text = loadingMessage
                content?.visibility = View.VISIBLE
            }
        }
    }

}