/*
 * Copyright (C) 2016 The Android Open Source Project
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
package jp.studyplus.android.app.jasper

import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import jp.studyplus.android.app.jasper.internal.bnv.MenuImpl
import jp.studyplus.android.app.jasper.internal.bnv.MenuView

class BottomNavigationView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private val menu: MenuImpl = MenuImpl(context) {
        if (reselectedListener == null || it.itemId != getSelectedItemId()) {
            return@MenuImpl selectedListener?.onNavigationItemSelected(it) ?: false
        }

        // item is already selected
        reselectedListener?.onNavigationItemReselected(it)
        return@MenuImpl true

    }
    private val menuView: MenuView = MenuView(context = context, menu = menu)

    private var selectedListener: OnNavigationItemSelectedListener? = null
    private var reselectedListener: OnNavigationItemReselectedListener? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationView, 0, 0).run {
            if (hasValue(R.styleable.BottomNavigationView_menu)) {
                MenuInflater(context).inflate(getResourceId(R.styleable.BottomNavigationView_menu, 0), menu)
            }

            menuView.iconTintList = if (hasValue(R.styleable.BottomNavigationView_itemIconTint)) {
                getColorStateList(R.styleable.BottomNavigationView_itemIconTint)
            } else {
                createDefaultColorStateList(android.R.attr.textColorSecondary)
            }
            menuView.itemTextColor = if (hasValue(R.styleable.BottomNavigationView_itemTextColor)) {
                getColorStateList(R.styleable.BottomNavigationView_itemTextColor)
            } else {
                createDefaultColorStateList(android.R.attr.textColorSecondary)
            }
            elevation = if (hasValue(R.styleable.BottomNavigationView_elevation)) {
                getDimensionPixelSize(R.styleable.BottomNavigationView_elevation, 0).toFloat()
            } else {
                resources.getDimension(R.dimen.bnv_elevation)
            }
            menuView.itemBackgroundRes = getResourceId(R.styleable.BottomNavigationView_itemBackground, 0)
            menuView.buildMenuView()

            recycle()
        }

        addView(menuView, LAYOUT_PARAMS)
    }

    /**
     * Set a listener that will be notified when a bottom navigation item is selected. This listener
     * will also be notified when the currently selected item is reselected, unless an
     * [OnNavigationItemReselectedListener] has also been set.
     *
     * @param listener The listener to notify
     *
     * @see setOnNavigationItemReselectedListener
     */
    fun setOnNavigationItemSelectedListener(listener: OnNavigationItemSelectedListener?) {
        selectedListener = listener
    }

    /**
     * Set a listener that will be notified when the currently selected bottom navigation item is
     * reselected. This does not require an [OnNavigationItemSelectedListener] to be set.
     *
     * @param listener The listener to notify
     *
     * @see setOnNavigationItemSelectedListener
     */
    fun setOnNavigationItemReselectedListener(listener: OnNavigationItemReselectedListener?) {
        reselectedListener = listener
    }

    /**
     * Returns the tint which is applied to our menu items' icons.
     *
     * @see setItemIconTintList
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    fun getItemIconTintList(): ColorStateList? {
        return menuView.iconTintList
    }

    /**
     * Set the tint which is applied to our menu items' icons.
     *
     * @param tint the tint to apply.
     *
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    fun setItemIconTintList(tint: ColorStateList?) {
        menuView.iconTintList = tint
    }

    /**
     * Returns colors used for the different states (normal, selected, focused, etc.) of the menu
     * item text.
     *
     * @see setItemTextColor
     * @return the ColorStateList of colors used for the different states of the menu items text.
     *
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    fun getItemTextColor(): ColorStateList? {
        return menuView.itemTextColor
    }

    /**
     * Set the colors to use for the different states (normal, selected, focused, etc.) of the menu
     * item text.
     *
     * @see getItemTextColor
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    fun setItemTextColor(textColor: ColorStateList?) {
        menuView.itemTextColor = textColor
    }

    /**
     * Returns the background resource of the menu items.
     *
     * @see setItemBackgroundResource
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    @DrawableRes
    fun getItemBackgroundResource(): Int {
        return menuView.itemBackgroundRes
    }

    /**
     * Set the background of our menu items to the given resource.
     *
     * @param resId The identifier of the resource.
     *
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    fun setItemBackgroundResource(@DrawableRes resId: Int) {
        menuView.itemBackgroundRes = resId
    }

    /**
     * Returns the currently selected menu item ID, or zero if there is no menu.
     *
     * @see setSelectedItemId
     */
    @IdRes
    fun getSelectedItemId(): Int {
        return menuView.selectedItemId
    }

    /**
     * Set the selected menu item ID. This behaves the same as tapping on an item.
     *
     * @param itemId The menu item ID. If no item has this ID, the current selection is unchanged.
     *
     * @see getSelectedItemId
     */
    fun setSelectedItemId(@IdRes itemId: Int) {
        menu.performIdentifierAction(itemId, 0)
    }

    /**
     * Listener for handling selection events on bottom navigation items.
     */
    interface OnNavigationItemSelectedListener {

        /**
         * Called when an item in the bottom navigation menu is selected.
         *
         * @param item The selected item
         *
         * @return true to display the item as the selected item and false if the item should not
         * be selected. Consider setting non-selectable items as disabled preemptively to
         * make them appear non-interactive.
         */
        fun onNavigationItemSelected(item: MenuItem): Boolean
    }

    /**
     * Listener for handling reselection events on bottom navigation items.
     */
    interface OnNavigationItemReselectedListener {

        /**
         * Called when the currently selected item in the bottom navigation menu is selected again.
         *
         * @param item The selected item
         */
        fun onNavigationItemReselected(item: MenuItem)
    }

    private fun createDefaultColorStateList(baseColorThemeAttr: Int): ColorStateList? {
        val value = TypedValue()
        if (!context.theme.resolveAttribute(baseColorThemeAttr, value, true)) {
            return null
        }
        val baseColor = AppCompatResources.getColorStateList(context, value.resourceId)
        if (!context.theme.resolveAttribute(R.attr.colorPrimary, value, true)) {
            return null
        }
        val colorPrimary = value.data
        val defaultColor = baseColor.defaultColor
        return ColorStateList(
            arrayOf(DISABLED_STATE_SET, CHECKED_STATE_SET, View.EMPTY_STATE_SET),
            intArrayOf(baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor)
        )
    }

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)

        private val LAYOUT_PARAMS = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}