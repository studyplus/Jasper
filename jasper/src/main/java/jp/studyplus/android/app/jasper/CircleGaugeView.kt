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
import android.graphics.Color
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

class CircleGaugeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val layout = LayoutInflater.from(context).inflate(R.layout.view_circle_gauge, this)
    private val text = layout.findViewById<TextView>(R.id.value_text)
    private val unit = layout.findViewById<TextView>(R.id.unit_text)
    private val ring = layout.findViewById<RingImageView>(R.id.ring_image)
}

internal class RingImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var centerX = 0F
    private var centerY = 0F
    private var ringStrokeWidth = 0F
    private var ringRadius = 0F
    private var innerRingStrokeWidth = 0F


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 描画中央値の確定
        centerX = (widthMeasureSpec / 2).toFloat()
        centerY = (heightMeasureSpec / 2).toFloat()
        ringStrokeWidth = (widthMeasureSpec / 4).toFloat()
        ringRadius = widthMeasureSpec / 2 - ringStrokeWidth
        innerRingStrokeWidth = ringStrokeWidth * 0.7F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // base ring
        // TODO: 仮値
        paint.color = Color.rgb(242, 242, 242)
        paint.strokeWidth = ringStrokeWidth
        paint.style = Paint.Style.STROKE
        canvas?.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint)

    }
}