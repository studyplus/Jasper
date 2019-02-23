package jp.studyplus.android.app.jasper

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class ChartView
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    protected val textSize: Int

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ChartView, 0, 0).run {
            textSize = getResourceId(
                R.styleable.ChartView_text_size,
                context.resources.getDimensionPixelSize(R.dimen.chart_default_text_size)
            )
            recycle()
        }
    }
}