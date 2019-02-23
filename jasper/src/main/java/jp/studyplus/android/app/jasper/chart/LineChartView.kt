package jp.studyplus.android.app.jasper.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import jp.studyplus.android.app.jasper.ChartView
import java.util.*

class LineChartView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ChartView(context, attrs, defStyleAttr) {

    private var MARGIN = 25.0f
    private var X_AXIS_AREA_SIZE = 55.0f
    private var Y_AXIS_AREA_SIZE = 35.0f
    private var AXIS_MARGIN = 4.0f

    private var POINT_SIZE = 2.0f
    private var BOLD_POINT_SIZE = 4.0f

    private var AXIS_FONT_SIZE = 12
    private var POINT_FONT_SIZE = 10

    private var initialized = false

    private var density: Float = 0.toFloat()
    private var textRect: Rect? = null
    private var paint: Paint? = null

    private var data: LineChartData? = null

    private var selectedSubjectKeys: MutableSet<String> = HashSet()

    constructor(context: Context) : super(context, null) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private fun init() {

        MARGIN *= density
        X_AXIS_AREA_SIZE *= density
        Y_AXIS_AREA_SIZE *= density
        AXIS_MARGIN *= density
        POINT_SIZE *= density
        BOLD_POINT_SIZE *= density
        AXIS_FONT_SIZE *= density.toInt()
        POINT_FONT_SIZE *= density.toInt()

        textRect = Rect()
        paint = Paint()
        paint!!.setAntiAlias(true)
    }

    fun setData(data: LineChartData) {
        val displayMetrics = DisplayMetricsUtils.getDisplayMetrics(getContext())
        val displayWidth = Math.round(displayMetrics.widthPixels / displayMetrics.density)

        val xAxisLabels = data.getXAxisLabels().size()
        if (xAxisLabels <= 0) {
            return  //FIXME データがないときは画像を出す
        }
        val layoutParams = getLayoutParams()
        if ((displayWidth - 60) / xAxisLabels < 50) {
            layoutParams.width = Math.round((60 + xAxisLabels * 50) * displayMetrics.density)
        } else {
            layoutParams.width = Math.round((displayWidth - 2) * displayMetrics.density)
        }
        this.setLayoutParams(layoutParams)
        this.data = data
    }

    fun setUseDeviation(useDeviation: Boolean) {
        if (null != data) {
            data!!.setUseDeviation(useDeviation)
            invalidate()
        }
    }

    fun setSelectedSubjectKeys(selectedSubjectKeys: MutableSet<String>) {
        this.selectedSubjectKeys = selectedSubjectKeys
        invalidate()
    }

    fun clearSelectedSubjectKeys() {
        this.selectedSubjectKeys.clear()
    }

    protected fun onDraw(canvas: Canvas) {
        if (paint != null) {
            if (null != data) {
                //折れ線グラフの点を置くことのできるX座標のリスト
                val (first, second) = writeBackground(canvas)
                writeScoreLine(canvas, first, second)
            }
        }
        super.onDraw(canvas)
    }

    //折れ線グラフの背景を描く
    //線を書くときに必要な交点を返す
    private fun writeBackground(canvas: Canvas): Pair<List<Float>, List<Float>> {
        val canvasWidth = canvas.getWidth()
        val canvasHeight = canvas.getHeight()

        // 1模試の横幅
        val examinationWidth = (canvasWidth - Y_AXIS_AREA_SIZE) / data!!.getXAxisLabels().size()

        // axis & line
        paint!!.setTextSize(AXIS_FONT_SIZE)

        val yPositionList = ArrayList()
        // yAxis & xLine
        run {
            val xPosition = Y_AXIS_AREA_SIZE
            var yPosition = canvasHeight.toFloat() - X_AXIS_AREA_SIZE - MARGIN
            val y = (yPosition - MARGIN) / data!!.getYDelta()
            var current = data!!.getYMin()
            while (current <= data!!.getYMax()) {
                // line
                paint!!.setColor(LINE_COLOR)
                canvas.drawLine(xPosition, yPosition, canvasWidth, yPosition, paint)

                // axis
                paint!!.setColor(TEXT_COLOR)
                val currentString = current.toString()
                paint!!.getTextBounds(currentString, 0, currentString.length, textRect)
                val textHeight = textRect!!.height()
                val textWidth = textRect!!.width()
                canvas.drawText(currentString, xPosition - textWidth - AXIS_MARGIN, yPosition + textHeight / 2, paint)

                yPositionList.add(yPosition)
                yPosition -= data!!.getYStep() * y
                current += data!!.getYStep()
            }
        }

        //グラフを書くときに使っていいX座標が欲しい
        //さもなくば同じ計算を線を描くときにもう一度しなくてはいけない
        val xPositionList = ArrayList()

        // xAxis & yLine
        run {
            var xPosition = Y_AXIS_AREA_SIZE + examinationWidth / 2
            val yPosition = canvasHeight - X_AXIS_AREA_SIZE

            for (strings in data!!.getXAxisLabels()) {
                // line
                paint!!.setColor(LINE_COLOR)
                canvas.drawLine(xPosition, 0, xPosition, yPosition, paint)

                // axis
                paint!!.setColor(TEXT_COLOR)

                // 1st line
                var firstLineHeight = 0f
                var secondLineHeight = 0f
                if (strings.size > 0 && !TextUtils.isEmpty(strings.get(0))) {
                    paint!!.getTextBounds(strings.get(0), 0, strings.get(0).length, textRect)
                    firstLineHeight = textRect!!.height()
                    val dateWidth = textRect!!.width()
                    canvas.drawText(
                        strings.get(0),
                        xPosition - dateWidth / 2,
                        yPosition + firstLineHeight + AXIS_MARGIN,
                        paint
                    )
                }
                // 2nd line
                if (strings.size > 1 && !TextUtils.isEmpty(strings.get(1))) {
                    paint!!.getTextBounds(strings.get(1), 0, strings.get(1).length, textRect)
                    secondLineHeight = textRect!!.height()
                    val nameWidth = textRect!!.width()
                    canvas.drawText(
                        strings.get(1),
                        xPosition - nameWidth / 2,
                        yPosition + firstLineHeight + secondLineHeight + AXIS_MARGIN * 2,
                        paint
                    )
                }
                // 3rd line
                if (strings.size > 2 && !TextUtils.isEmpty(strings.get(2))) {
                    paint!!.getTextBounds(strings.get(2), 0, strings.get(2).length, textRect)
                    val thirdLineHeight = textRect!!.height()
                    val nameWidth = textRect!!.width()
                    canvas.drawText(
                        strings.get(2),
                        xPosition - nameWidth / 2,
                        yPosition + firstLineHeight + secondLineHeight + thirdLineHeight + AXIS_MARGIN * 3,
                        paint
                    )
                }

                xPositionList.add(xPosition)
                xPosition += examinationWidth
            }
        }

        return Pair.create(xPositionList, yPositionList)
    }

    //線グラフにするとき、使っていい領域の計算や点をつけるx,y座標などが欲しいので引数でとる
    private fun writeScoreLine(canvas: Canvas, xPositionList: List<Float>, yPositionList: List<Float>) {
        val linePaint = Paint()
        linePaint.setAntiAlias(true)
        linePaint.setStyle(Paint.Style.STROKE)

        val pointPaint = Paint()
        pointPaint.setAntiAlias(true)
        pointPaint.setStyle(Paint.Style.FILL)

        val scorePaint = Paint()
        scorePaint.setAntiAlias(true)
        scorePaint.setStyle(Paint.Style.FILL_AND_STROKE)
        scorePaint.setStrokeWidth(1)
        scorePaint.setTextSize(POINT_FONT_SIZE)

        val scoreFatPaint = Paint()
        scoreFatPaint.setAntiAlias(true)
        scoreFatPaint.setStrokeWidth(3)
        scoreFatPaint.setStyle(Paint.Style.FILL_AND_STROKE)
        scoreFatPaint.setColor(Color.WHITE)
        scoreFatPaint.setTextSize(POINT_FONT_SIZE)

        val lines = data!!.getGraphLines()
        // 細い線
        for (line in lines) {
            if (!selectedSubjectKeys.contains(line.getSubjectKey())) {
                val coordinates = lineToCoordinates(canvas, xPositionList, yPositionList, line)
                if (coordinates.size > 0) {
                    linePaint.setColor(line.getLineColor())
                    linePaint.setStrokeWidth(1 * density)
                    pointPaint.setColor(line.getLineColor())
                    pointPaint.setStrokeWidth(0)

                    writeLineByCoordinates(canvas, coordinates, pointPaint, linePaint, false)
                }
            }
        }

        // 太い線
        for (line in lines) {
            if (selectedSubjectKeys.contains(line.getSubjectKey())) {
                val coordinates = lineToCoordinates(canvas, xPositionList, yPositionList, line)
                if (coordinates.size > 0) {
                    linePaint.setColor(line.getLineColor())
                    linePaint.setStrokeWidth(3 * density)
                    pointPaint.setColor(Color.WHITE)
                    pointPaint.setStrokeWidth(0)

                    writeLineByCoordinates(canvas, coordinates, pointPaint, linePaint, true)
                }

                for (coordinate in coordinates) {
                    canvas.drawText(
                        coordinate.getLabel(),
                        coordinate.getX() + AXIS_MARGIN,
                        coordinate.getY() - AXIS_MARGIN,
                        scoreFatPaint
                    )
                    canvas.drawText(
                        coordinate.getLabel(),
                        coordinate.getX() + AXIS_MARGIN,
                        coordinate.getY() - AXIS_MARGIN,
                        scorePaint
                    )
                }
            }
        }
    }

    private fun writeLineByCoordinates(
        canvas: Canvas,
        coordinates: List<Coordinate>,
        pointPaint: Paint,
        linePaint: Paint,
        isBold: Boolean
    ) {
        val path = Path()

        // line
        path.moveTo(coordinates[0].getX(), coordinates[0].getY())
        for (i in 1 until coordinates.size) {
            path.lineTo(coordinates[i].getX(), coordinates[i].getY())
        }
        canvas.drawPath(path, linePaint)

        // point
        for (coordinate in coordinates) {
            if (isBold) {
                canvas.drawCircle(coordinate.getX(), coordinate.getY(), BOLD_POINT_SIZE, pointPaint)
                canvas.drawCircle(coordinate.getX(), coordinate.getY(), BOLD_POINT_SIZE, linePaint)
            } else {
                canvas.drawCircle(coordinate.getX(), coordinate.getY(), POINT_SIZE, pointPaint)
            }
        }
    }

    private fun lineToCoordinates(
        canvas: Canvas,
        xPositionList: List<Float>,
        yPositionList: List<Float>,
        line: LineChartData.GraphLine
    ): List<Coordinate> {
        val origin = getOrigin(canvas)
        val values = line.getValues()
        val coordinates = ArrayList()
        val minimumY = data!!.getYMin()
        for (i in 0 until line.getValues().size()) {
            val value = values.get(i)
            if (null != value) {
                val x = xPositionList[i]
                //表示されるスコア領域に対してこのスコアが締める割合
                val scorePercentage = (value!! - minimumY) / data!!.getYDelta()
                //スコアの表示に使って良い領域の高さ
                //背景に登場するy座標のなかから一番大きなもの(つまりandroid的には小さなもの)と原点の差がそれ
                val scoreYLength = Math.abs(Collections.min(yPositionList) - origin.getY())
                //実際のY座標は、表示領域の高さ掛けるスコア割合を原点のY座標から引いたものになる
                val y = (origin.getY() - scorePercentage * scoreYLength) as Float
                val coordinate = Coordinate(x, y)
                coordinate.setLabel(value)
                coordinates.add(coordinate)
            }
        }
        return coordinates
    }

    //グラフの原点を取得する
    private fun getOrigin(canvas: Canvas): Coordinate {
        return Coordinate(Y_AXIS_AREA_SIZE, canvas.getHeight() - X_AXIS_AREA_SIZE - MARGIN)
    }

    companion object {
        private val TEXT_COLOR = Color.argb(138, 0, 0, 0)
        private val LINE_COLOR = Color.rgb(216, 216, 216)
    }
}