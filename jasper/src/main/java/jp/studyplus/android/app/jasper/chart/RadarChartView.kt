package jp.studyplus.android.app.jasper.chart

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.BounceInterpolator
import jp.studyplus.android.app.jasper.ChartView
import java.util.*

class RadarChartView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ChartView(context, attrs, defStyleAttr) {

    private var data: RadarChartData? = null

    private var animationProgress: Float = 0.toFloat()

    private var scoreLineColor: Int = 0

    private var scoreFillColor: Int = 0

    private var useDeviation: Boolean = false

    fun setData(data: RadarChartData, useDeviation: Boolean) {
        this.data = data
        this.animationProgress = 0f
        if (useDeviation) {
            this.scoreLineColor = Color.argb(150, 74, 144, 226)
            this.scoreFillColor = Color.argb(50, 73, 143, 225)
        } else {
            this.scoreLineColor = Color.argb(150, 170, 213, 94)
            this.scoreFillColor = Color.argb(50, 170, 213, 94)
        }
        this.useDeviation = useDeviation
        val animator = ObjectAnimator.ofFloat(this, "progress", 0.5f, 1f)
        animator.duration = 1000
        animator.interpolator = BounceInterpolator()
        animator.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (null != data) {
            val width = canvas.width
            val height = canvas.height
            val center = Coordinate(width / 2, height / 2)

            val textSize = (LABEL_SIZE * density).toDouble()
            //TODO マジックナンバー
            val radius = Math.min(width - textSize * 8, height - textSize * 2) / 2 * 0.8

            val lineColor = Color.argb(200, 100, 100, 100)
            val (first, second) = drawBackground(canvas, center, radius.toFloat(), lineColor, data!!)

            if (0 <= animationProgress) {
                drawScorePolygon(canvas, center, radius.toFloat(), data!!, first, second, animationProgress.toDouble())
            }
        }
        super.onDraw(canvas)
    }

    // グラフの背景を描く
    // スコアを描くために最小、最大値を返す
    private fun drawBackground(
        canvas: Canvas,
        center: Coordinate,
        radius: Float,
        lineColor: Int,
        data: RadarChartData
    ): Pair<Int, Int> {
        val defaultMin = if (useDeviation) 20 else 0
        val defaultMax = if (useDeviation) 80 else 100
        val layers = data.getScoreLayers(10, defaultMin, defaultMax, !useDeviation)

        val linePaint = Paint()
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.color = lineColor

        var index = 0.0
        for (layer in layers) {
            val ratio = index++ / (layers.size - 1)
            val isThickLine = 50 == layer
            drawCircle(canvas, center, (radius * ratio).toFloat(), lineColor, GRAPH_BACKGROUND_COLOR, isThickLine)
        }

        val labels = data.getLabels()
        val scorePaint = Paint()
        scorePaint.isAntiAlias = true
        scorePaint.style = Paint.Style.STROKE
        scorePaint.color = Color.argb(250, 158, 158, 158)
        scorePaint.textSize = LABEL_SIZE * density

        val regularVertices = getOuterRegularVertices(center, radius, labels)
        for (coordinate in regularVertices) {
            canvas.drawLine(center.getX(), center.getY(), coordinate.getX(), coordinate.getY(), linePaint)
        }

        if (regularVertices.size < 1) {
            drawLabel(notEnoughSubjectsWarning, canvas, Coordinate(center.getX(), center.getY()), scorePaint)
        }

        // 円を描き終わったあとに点数を書かないとかぶって見えにくくなってしまうので２度手間をかけている
        index = 0.0
        for (layer in layers) {
            if (index == 0.0 || index == (layers.size - 1).toDouble() || layer == 50) {
                val ratio = index / (layers.size - 1)
                drawScoreThresholdLabels(canvas, center, (radius * ratio).toFloat(), layer!!.toString(), lineColor)
            }
            index++
        }

        val labelVertices = getOuterRegularVertices(center, radius, labels, 1.1)
        for (coordinate in labelVertices) {
            val adjusted = hoge(coordinate, center, radius.toDouble(), coordinate.getLabel(), scorePaint, canvas)
            drawLabel(coordinate.getLabel(), canvas, adjusted, scorePaint)
        }

        return Pair.create(Collections.min<Int>(layers), Collections.max<Int>(layers))
    }

    // 得点の多角形を描く
    private fun drawScorePolygon(
        canvas: Canvas,
        center: Coordinate,
        radius: Float,
        data: RadarChartData,
        min: Int,
        max: Int,
        progress: Double
    ) {
        val vertices =
            getVertices(center, radius, data.getRatios(min.toDouble()), data.getScoreLabels(!useDeviation), progress)
        val scorePath = getPolygonPath(vertices)

        val labelPaint = Paint()
        labelPaint.isAntiAlias = true
        labelPaint.color = Color.BLACK
        labelPaint.style = Paint.Style.STROKE
        labelPaint.textSize = LABEL_SIZE * density

        for (coordinate in vertices) {
            if (null != coordinate.getLabel()) {
                drawLabel(coordinate.getLabel(), canvas, coordinate, labelPaint)
            }
        }
        drawPath(canvas, scorePath, scoreLineColor, 3, scoreFillColor)
    }

    private fun getPolygonPath(vertices: List<Coordinate>): Path {
        val path = Path()
        assert(3 <= vertices.size)
        //最低三つの頂点がないとレーダー描けない
        if (vertices.size < 3) {
            return path
        }
        val first = vertices[0]
        path.moveTo(first.getX(), first.getY())
        for (vertex in vertices.subList(1, vertices.size)) {
            path.lineTo(vertex.getX(), vertex.getY())
        }
        path.lineTo(first.getX(), first.getY())
        path.close()
        return path
    }

    private fun drawPath(canvas: Canvas, path: Path, lineColor: Int, strokeWidth: Int, fillColor: Int) {
        val paint = Paint()
        paint.isAntiAlias = true

        paint.style = Paint.Style.STROKE
        paint.color = lineColor
        paint.strokeWidth = strokeWidth.toFloat()
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawPath(path, paint)
    }

    private fun drawCircle(
        canvas: Canvas,
        center: Coordinate,
        radius: Float,
        lineColor: Int,
        fillColor: Int,
        isThickLine: Boolean
    ) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = lineColor

        if (isThickLine) {
            paint.strokeWidth = 3f
        } else {
            paint.strokeWidth = 1f
        }

        canvas.drawCircle(center.getX(), center.getY(), radius, paint)

        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawCircle(center.getX(), center.getY(), radius, paint)
    }

    // 返すのはラベルの中心にしたい座標
    private fun hoge(
        coordinate: Coordinate,
        center: Coordinate,
        radius: Double,
        text: String,
        paint: Paint,
        canvas: Canvas
    ): Coordinate {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)

        val margin = (30 * density).toDouble()
        val closest = coordinate.getClosestVertex(center, rect.width() + margin, rect.height())
        canvas.drawPoint(closest.getX(), closest.getY(), paint)

        val distance = closest.distance(center)
        val delta = radius - distance
        return if (0 < delta) {
            coordinate.stretch(center, delta)
        } else {
            coordinate
        }
    }

    private fun drawScoreThresholdLabels(
        canvas: Canvas,
        center: Coordinate,
        radius: Float,
        label: String,
        lineColor: Int
    ) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = lineColor
        paint.textSize = 8 * density//TODO 適当
        //得点を表す背景の目盛り
        drawLabel(label, canvas, Coordinate(center.getX(), center.getY() - radius), paint)
    }

    //指定された座標が中心に来るようにラベルを書く
    private fun drawLabel(text: String?, canvas: Canvas, coordinate: Coordinate, paint: Paint) {
        val rect = Rect()
        paint.getTextBounds(text, 0, text!!.length, rect)

        val x = coordinate.getX() - rect.width() / 2
        val y = coordinate.getY() + rect.height() / 2

        val backGround = Paint()
        backGround.style = Paint.Style.FILL
        backGround.color = Color.argb(255, 250, 250, 250)
        canvas.drawRect(x, y - rect.height(), x + rect.width(), y, backGround)

        canvas.drawText(text, x, y, paint)
    }

    private fun getOuterRegularVertices(
        center: Coordinate,
        radius: Float,
        labels: List<String>,
        ratio: Double = 1.0
    ): List<Coordinate> {
        val ratios = getRegularRatios(labels.size, ratio)
        return getVertices(center, radius, ratios, labels, ratio)
    }

    // 多角形の頂点を取得する
    // グラフの背景にある正n角形の各頂点から中心への距離を１とした場合の割合を指定する
    private fun getVertices(
        center: Coordinate,
        radius: Float,
        ratios: List<Double>,
        labels: List<String>,
        animationProgress: Double
    ): List<Coordinate> {
        assert(ratios.size == labels.size)
        val angle = 2 * Math.PI / ratios.size
        var index = 0
        val list = ArrayList<Coordinate>()
        val labelIterator = labels.iterator()
        for (ratio in ratios) {
            val length = radius.toDouble() * ratio * animationProgress
            val arcAngle = angle * index
            val x = center.getX() - length * Math.sin(arcAngle)
            val y = center.getY() + length * Math.cos(arcAngle)
            val coordinate = Coordinate(x.toFloat(), y.toFloat()).inverse(center)
            if (labelIterator.hasNext()) {
                coordinate.setLabel(labelIterator.next())
            }
            list.add(coordinate)
            index += 1
        }
        return list
    }

    // 正n角形の頂点が欲しいときに使う
    private fun getRegularRatios(numberOfVertices: Int, ratio: Double): List<Double> {
        val ratios = ArrayList<Double>()
        for (i in 0 until numberOfVertices) {
            ratios.add(ratio)
        }
        return ratios
    }

    //ObjectAnimatorから呼ばれる
    private fun setProgress(progress: Float) {
        this.animationProgress = progress
        invalidate()
    }

    companion object {

        private val LABEL_SIZE = 10

        private val GRAPH_BACKGROUND_COLOR = Color.argb(0, 150, 150, 150)
    }

}
