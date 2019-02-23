package jp.studyplus.android.app.jasper.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import jp.studyplus.android.app.jasper.ChartView
import jp.studyplus.android.app.jasper.R

class BarChartView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ChartView(context, attrs, defStyleAttr) {

    private val outlineWidth: Float
    private val axisMargin = context.resources.getDimension(R.dimen.bar_chart_default_axis_margin)
    private val labelMargin = context.resources.getDimension(R.dimen.bar_chart_default_label_margin)

    private var barChartData: BarChartData? = null

    // for draw
    private val textRect: Rect = Rect()
    private val paint: Paint = Paint().apply {
        isAntiAlias = true

    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BarChartView, 0, 0).run {
            outlineWidth = getDimension(
                R.styleable.BarChartView_outline_width,
                context.resources.getDimension(R.dimen.bar_chart_default_outline_size)
            )
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw outline

       val data = barChartData ?: return
        // draw chart Data

        barChartData?.let {
            // X Axis Height
            var xAxisHeight = 0f
            if (it.xAxis != null) {
                for (axis in it.xAxis) {
                    var height = 0f
                    if (axis != null) {
                        for (axisText in axis) {
                            paint.getTextBounds(axisText, 0, axisText.length, textRect)
                            height += textRect.height().toFloat()
                        }
                    }
                    xAxisHeight = Math.max(height, xAxisHeight)
                }
            }

            // Y Axis Width
            var yAxisWidth = 0f
            if (it.yAxis != null) {
                for (axis in it.yAxis) {
                    var width = 0f
                    if (axis != null) {
                        paint.getTextBounds(axis.first, 0, axis.first?.length ?: 0, textRect)
                        width += textRect.width().toFloat()
                    }
                    yAxisWidth = Math.max(width, yAxisWidth)
                }
            }

            // label Height
            paint.textAlign = Paint.Align.CENTER
            var labelHeight = 0f
            if (it.datas != null) {
                for (i in it.datas.indices) {
                    val labels = it.labels[i]
                    var height = 0f
                    for (j in labels.size downTo 1) {
                        val labelText = labels[j - 1]
                        paint.getTextBounds(labelText, 0, labelText.length, textRect)
                        height += textRect.height().toFloat()
                    }
                    labelHeight = Math.max(height, labelHeight)
                }
            }

            // Calc
            val yZeroPoint: Float = canvas.height - (xAxisHeight + axisMargin)
            val yUnit: Float = if (it.yMax == 0) {
                0f
            } else {
                (yZeroPoint - labelHeight - labelMargin) / it.yMax
            }
            val areaWidth: Float = (canvas.width - (yAxisWidth + axisMargin)) / it.xAxis.size
            val barWidth: Float = areaWidth / 2

            // Bar and Label
            if (it.datas != null && it.colors != null) {
                for (i in it.datas.indices) {
                    val data = it.datas[i]
                    var prevY = yZeroPoint
                    if (data != null) {
                        for (j in data.indices) {
                            val color = it.colors[j % it.colors.size]

                            val startX = yAxisWidth + axisMargin + i * areaWidth + areaWidth / 2 - barWidth / 2
                            val height = yUnit * data[j]
                            val startY = prevY - height

                            paint.color = color
                            canvas.drawRect(startX, startY, startX + barWidth, startY + height, paint)

                            prevY = startY
                        }
                    }
                    // Label
                    paint.color = LABEL_COLOR
                    paint.textAlign = Paint.Align.CENTER

                    prevY -= labelMargin

                    val labels = it.labels[i]
                    for (j in labels.size downTo 1) {
                        val labelText = labels[j - 1]
                        paint.getTextBounds(labelText, 0, labelText.length, textRect)
                        canvas.drawText(
                            labelText,
                            yAxisWidth + axisMargin + areaWidth / 2 + areaWidth * i,
                            prevY,
                            paint
                        )
                        prevY -= textRect.height().toFloat()
                    }
                }
            }

            // outline
            paint.color = OUTLINE_COLOR
            paint.strokeWidth = outlineWidth
            canvas.drawLine(
                yAxisWidth + axisMargin,
                0f,
                yAxisWidth + axisMargin,
                canvas.height - xAxisHeight,
                paint
            )
            for (axis in it.yAxis) {
                canvas.drawLine(
                    yAxisWidth,
                    yZeroPoint - yUnit * (axis.second ?: 0),
                    canvas.width.toFloat(),
                    yZeroPoint - yUnit * (axis.second ?: 0),
                    paint
                )
            }

            // X Axis Label
            paint.color = AXIS_LABEL_COLOR
            paint.textAlign = Paint.Align.CENTER
            if (it.xAxis != null) {
                for (i in it.xAxis.indices) {
                    val axis = it.xAxis[i]
                    if (axis != null) {
                        val y = yZeroPoint + axisMargin
                        for (j in axis.indices) {
                            paint.getTextBounds(axis[j], 0, axis[j].length, textRect)
                            canvas.drawText(
                                axis[j],
                                yAxisWidth + axisMargin + areaWidth / 2 + areaWidth * i,
                                y + textRect.height(),
                                paint
                            )
                        }
                    }
                }
            }

            // Y Axis Label
            paint.color = AXIS_LABEL_COLOR
            paint.textAlign = Paint.Align.RIGHT
            if (it.yAxis != null) {
                for (i in it.yAxis.indices) {
                    val axis = it.yAxis[i]
                    if (axis != null) {
                        val y = yZeroPoint - yUnit * (axis.second ?: 0)
                        val axisText = axis.first
                        paint.getTextBounds(axisText, 0, axisText?.length ?: 0, textRect)
                        canvas.drawText(axisText, yAxisWidth, y + textRect.height() / 2, paint)
                    }
                }
            }
        }
    }

    fun setData(barChartData: BarChartData) {
        this.barChartData = barChartData
        invalidate()
    }

    companion object {
        private val OUTLINE_COLOR = Color.argb(Math.round(0.54 * 255).toInt(), 0, 0, 0)
        private val AXIS_LABEL_COLOR = Color.argb(Math.round(0.54 * 255).toInt(), 0, 0, 0)
        private val LABEL_COLOR = Color.argb(Math.round(0.87 * 255).toInt(), 0, 0, 0)
    }
}
