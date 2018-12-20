/*
 * Copyright (c) 2018 Studyplus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.studyplus.android.app.jasper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.SweepGradient
import android.support.annotation.ColorInt
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView

class CircleGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val layout = LayoutInflater.from(context).inflate(R.layout.view_circle_gauge, this)
    private val value = layout.findViewById<TextView>(R.id.value_text)
    private val unit = layout.findViewById<TextView>(R.id.unit_text)
    private val ring = layout.findViewById<RingImageView>(R.id.ring_image)

    init {
        // TODO: 仮値
        value.text = "100"
        unit.text = "%"
    }
}

internal class RingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint()

    private var centerX = 0F
    private var centerY = 0F
    private var ringStrokeWidth = 0F
    private var ringRadius = 0F
    private var innerRingStrokeWidth = 0F
    private var rainbowShader: Shader? = null

    private val rainbowColors by lazy { rainbow(context) }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 描画中央値の確定
        centerX = (measuredWidth / 2).toFloat()
        centerY = (measuredHeight / 2).toFloat()
        ringStrokeWidth = (measuredWidth / 4).toFloat()
        ringRadius = measuredWidth / 2 - ringStrokeWidth
        innerRingStrokeWidth = ringStrokeWidth * 0.7F

        rainbowShader = SweepGradient(centerX, centerY, rainbowColors, null)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // base ring
        // TODO: 仮値
        paint.shader = rainbowShader
        paint.strokeWidth = ringStrokeWidth
        paint.style = Paint.Style.STROKE
        canvas?.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint)

    }


    // TODO: 既存から色配置をそのまま持ってきているので番号が不揃い
    @ColorInt
    fun rainbow(context: Context): IntArray {
        return intArrayOf(
            ContextCompat.getColor(context, R.color.rainbow_2),
            ContextCompat.getColor(context, R.color.rainbow_3),
            ContextCompat.getColor(context, R.color.rainbow_4),
            ContextCompat.getColor(context, R.color.rainbow_5),
            ContextCompat.getColor(context, R.color.rainbow_6),
            ContextCompat.getColor(context, R.color.rainbow_7),
            ContextCompat.getColor(context, R.color.rainbow_8),
            ContextCompat.getColor(context, R.color.rainbow_9),
            ContextCompat.getColor(context, R.color.rainbow_10),
            ContextCompat.getColor(context, R.color.rainbow_0),
            ContextCompat.getColor(context, R.color.rainbow_1),
            ContextCompat.getColor(context, R.color.rainbow_2)
        )
    }
}