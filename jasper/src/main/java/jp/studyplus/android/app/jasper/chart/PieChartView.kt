package jp.studyplus.android.app.jasper.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import jp.studyplus.android.app.jasper.ChartView

class PieChartView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ChartView(context, attrs, defStyleAttr) {

    private val labelSize = 10f * density

    private lateinit var pieChartData: List<PieChartData>

    // for draw
    private val paint: Paint = Paint()
    private val labelRect: Rect = Rect()
    private val rectF: RectF = RectF()

    init {
        // label setting
        paint.isAntiAlias = true
        paint.textSize = labelSize
        paint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        if (::pieChartData.isInitialized) {
            // center
            val centerX = (canvas.width / 2).toFloat()
            val centerY = (canvas.height / 2).toFloat()

            // radius
            val r = (Math.min(centerX, centerY) * 0.9).toFloat()

            rectF.set(centerX - r, centerY - r, centerX + r, centerY + r)

            var prevDeg = -90f

            pieChartData.forEach { data ->
                paint.color = data.color
                paint.style = Paint.Style.FILL

                canvas.drawArc(rectF, prevDeg, 360f * data.value, true, paint)

                // label
                if (data.value > 0.1f) {
                    val label = String.format("%.1f%%", data.value * 100)
                    paint.getTextBounds(label, 0, label.length, labelRect)

                    val x =
                        (centerX + Math.cos((prevDeg + 360f * data.value / 2) / 180 * Math.PI) * r.toDouble() * 0.6).toFloat()
                    val y =
                        (centerY.toDouble() + Math.sin((prevDeg + 360f * data.value / 2) / 180 * Math.PI) * r.toDouble() * 0.6 + labelRect.height() * 0.5).toFloat()

                    paint.color = LABEL_COLOR
                    canvas.drawText(label, x, y, paint)
                }

                prevDeg += 360f * data.value
            }
        }
        super.onDraw(canvas)
    }

    fun setData(pieChartData: List<PieChartData>) {
        this.pieChartData = pieChartData
        invalidate()
    }

    companion object {
        private val LABEL_COLOR = Color.argb(Math.round(0.54 * 255).toInt(), 0, 0, 0)
    }
}