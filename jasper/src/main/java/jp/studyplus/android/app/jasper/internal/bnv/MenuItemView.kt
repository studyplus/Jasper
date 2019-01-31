/*
 * Copyright (c) 2019 Studyplus, Inc.
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
package jp.studyplus.android.app.jasper.internal.bnv

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.PointerIconCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.TooltipCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import jp.studyplus.android.app.jasper.R

internal class MenuItemView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), Checkable {

    private val icon: ImageView by lazy { findViewById<ImageView>(R.id.icon) }
    private val label: TextView by lazy { findViewById<TextView>(R.id.label) }

    private var itemData: MenuItemImpl? = null
    private var iconTint: ColorStateList? = null
    private var checked: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.bnv_menu_item, this, true)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (itemData?.isCheckable == true && itemData?.isChecked == true) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        label.isEnabled = enabled
        icon.isEnabled = enabled

        if (enabled) {
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(context, PointerIconCompat.TYPE_HAND))
        } else {
            ViewCompat.setPointerIcon(this, null)
        }
    }

    override fun isChecked(): Boolean = checked

    override fun toggle() {
        checked = checked.not()
    }

    override fun setChecked(checked: Boolean) {
        this.checked = checked
    }

    fun setItemData(itemData: MenuItemImpl) {
        this.itemData = itemData

        id = itemData.itemId
        setTitle(itemData.title)
        setIcon(itemData.icon)
        isEnabled = itemData.isEnabled
        checked = itemData.isChecked

        contentDescription = itemData.contentDescription
        TooltipCompat.setTooltipText(this, itemData.tooltipText)
    }

    fun getItemData(): MenuItemImpl? {
        return itemData
    }

    fun setTitle(title: CharSequence?) {
        if (title.isNullOrEmpty()) {
            label.text = null
            label.visibility = GONE
        } else {
            label.text = title
        }
    }

    fun setIcon(icon: Drawable?) {
        if (icon == null) {
            this.icon.setImageDrawable(null)
            return
        }

        val state = icon.constantState
        val drawable = DrawableCompat.wrap(if (state == null) icon else state.newDrawable()).mutate()
        DrawableCompat.setTintList(drawable, iconTint)

        this.icon.setImageDrawable(drawable)
    }

    fun setIconTintList(tint: ColorStateList?) {
        iconTint = tint
        setIcon(itemData?.icon)
    }

    fun setTextColor(color: ColorStateList?) {
        label.setTextColor(color)
    }

    fun setItemBackground(background: Int) {
        val backgroundDrawable = if (background != 0) {
            ContextCompat.getDrawable(context, background)
        } else {
            null
        }
        ViewCompat.setBackground(this, backgroundDrawable)
    }

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
}
