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
import android.support.v4.util.Pools
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout

internal class MenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val menu: MenuImpl
) : LinearLayout(context, attrs, defStyleAttr) {

    var selectedItemId = 0
        private set

    var iconTintList: ColorStateList? = null
        set(tint) {
            field = tint
            if (itemViewList.isEmpty()) return
            itemViewList.forEach { it.setIconTintList(tint) }
        }

    var itemTextColor: ColorStateList? = null
        set(color) {
            field = color
            if (itemViewList.isEmpty()) return
            itemViewList.forEach { it.setTextColor(color) }
        }

    var itemBackgroundRes: Int = 0
        set(background) {
            field = background
            if (itemViewList.isEmpty()) return
            itemViewList.forEach { it.setItemBackground(background) }
        }

    private val itemViewList = mutableListOf<MenuItemView>()
    private val itemPool = Pools.SynchronizedPool<MenuItemView>(MAX_ITEM_VIEW_SIZE)
    private val newItem: MenuItemView
        get() {
            var item = itemPool.acquire()
            if (item == null) {
                item = MenuItemView(context)
            }
            return item
        }

    private var selectedItemPosition = 0

    fun buildMenuView() {
        removeAllViews()
        itemViewList.forEach { itemPool.release(it) }
        itemViewList.clear()

        if (menu.size() == 0) {
            selectedItemId = 0
            selectedItemPosition = 0
            return
        }

        for (i in 0 until menu.size()) {
            menu.getItem(i).isCheckable = true
            menu.getItem(i).isChecked = false

            val child = newItem
            child.setIconTintList(iconTintList)
            child.setTextColor(itemTextColor)
            child.setItemBackground(itemBackgroundRes)
            child.setItemData(menu.getItem(i) as MenuItemImpl)
            child.setOnClickListener { v ->
                val itemView = v as MenuItemView
                val item = itemView.getItemData()
                val itemId = item?.itemId ?: return@setOnClickListener

                if (menu.performIdentifierAction(itemId, 0)) {
                    selectedItemId = itemId
                    updateMenuView()
                }
            }

            itemViewList.add(i, child)
            addView(child, LAYOUT_PARAM)
        }

        selectedItemPosition = Math.min(menu.size() - 1, selectedItemPosition)
        menu.getItem(selectedItemPosition).isChecked = true
    }

    fun updateMenuView() {
        val menuSize = menu.size()
        if (menuSize != itemViewList.size) {
            // The size has changed. Rebuild menu view from scratch.
            buildMenuView()
            return
        }

        val previousSelectedId = selectedItemId
        for (i in 0 until menuSize) {
            val item = menu.getItem(i)
            if (item.isChecked) {
                selectedItemId = item.itemId
                selectedItemPosition = i
            }
        }
        if (previousSelectedId != selectedItemId) {
            // Note: this has to be called before MenuItemView#setItemData().
            val transitionSet: TransitionSet = AutoTransition().apply {
                setOrdering(TransitionSet.ORDERING_TOGETHER)
                setDuration(ACTIVE_ANIMATION_DURATION_MS)
                setInterpolator(FastOutSlowInInterpolator())
            }
            TransitionManager.beginDelayedTransition(this, transitionSet)
        }

        for (i in 0 until menuSize) {
            itemViewList[i].setItemData(menu.getItem(i) as MenuItemImpl)
            itemViewList[i].refreshDrawableState()
        }
    }

    companion object {
        private const val MAX_ITEM_VIEW_SIZE = 5
        private const val ACTIVE_ANIMATION_DURATION_MS = 115L

        private val LAYOUT_PARAM = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F
        )
    }
}
