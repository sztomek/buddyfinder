package hu.sztomek.wheresmybuddy.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView


class VerticalTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas?) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState

        canvas?.let {
            canvas.save()
            canvas.translate(0f, height.toFloat())
            canvas.rotate(-90f)
            canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
            layout.draw(canvas)
            canvas.restore()
        }
    }

}