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
import android.os.Parcel
import android.os.Parcelable
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

    val menu: MenuImpl = MenuImpl(context) {
        if (onNavigationItemReselectedListener == null || it.itemId != selectedItemId) {
            return@MenuImpl onNavigationItemSelectedListener?.invoke(it) ?: false
        }

        // item is already selected
        onNavigationItemReselectedListener?.invoke(it)
        return@MenuImpl true

    }
    // region menu view
    private val menuView: MenuView = MenuView(context = context, menu = menu)

    var itemIconTintList
        /**
         * Returns the tint which is applied to our menu items' icons.
         *
         * @see setItemIconTintList
         * @attr ref R.styleable#BottomNavigationView_itemIconTint
         */
        get() = menuView.iconTintList
        /**
         * Set the tint which is applied to our menu items' icons.
         *
         * @param tint the tint to apply.
         *
         * @attr ref R.styleable#BottomNavigationView_itemIconTint
         */
        set(value) {
            menuView.iconTintList = value
        }

    var itemTextColor
        /**
         * Returns colors used for the different states (normal, selected, focused, etc.) of the menu
         * item text.
         *
         * @see setItemTextColor
         * @return the ColorStateList of colors used for the different states of the menu items text.
         *
         * @attr ref R.styleable#BottomNavigationView_itemTextColor
         */
        get() = menuView.itemTextColor
        /**
         * Set the colors to use for the different states (normal, selected, focused, etc.) of the menu
         * item text.
         *
         * @see getItemTextColor
         * @attr ref R.styleable#BottomNavigationView_itemTextColor
         */
        set(value) {
            menuView.itemTextColor = value
        }

    var itemBackgroundResource
        /**
         * Returns the background resource of the menu items.
         *
         * @see setItemBackgroundResource
         * @attr ref R.styleable#BottomNavigationView_itemBackground
         */
        @DrawableRes
        get() = menuView.itemBackgroundRes
        /**
         * Set the background of our menu items to the given resource.
         *
         * @param resId The identifier of the resource.
         *
         * @attr ref R.styleable#BottomNavigationView_itemBackground
         */
        set(@DrawableRes value) {
            menuView.itemBackgroundRes = value
        }

    var selectedItemId
        /**
         * Returns the currently selected menu item ID, or zero if there is no menu.
         *
         * @see setSelectedItemId
         */
        @IdRes
        get() = menuView.selectedItemId
        /**
         * Set the selected menu item ID. This behaves the same as tapping on an item.
         *
         * @param itemId The menu item ID. If no item has this ID, the current selection is unchanged.
         *
         * @see getSelectedItemId
         */
        set(@IdRes value) {
            menu.performIdentifierAction(value, 0)
        }
    // endregion

    /**
     * Called when an item in the bottom navigation menu is selected.
     *
     * @param item The selected item
     *
     * @return true to display the item as the selected item and false if the item should not
     * be selected. Consider setting non-selectable items as disabled preemptively to
     * make them appear non-interactive.
     */
    var onNavigationItemSelectedListener: ((item: MenuItem) -> Boolean)? = null
    /**
     * Called when the currently selected item in the bottom navigation menu is selected again.
     *
     * @param item The selected item
     */
    var onNavigationItemReselectedListener: ((item: MenuItem) -> Unit)? = null

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

    override fun onSaveInstanceState(): Parcelable? {
        val parent = super.onSaveInstanceState() as Parcelable
        val saved = SavedState(parent)
        saved.selectedItemId = selectedItemId
        return saved
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val saved = state as SavedState
        super.onRestoreInstanceState(saved.superState)
        selectedItemId = saved.selectedItemId
    }

    private class SavedState : BaseSavedState {
        var selectedItemId: Int = -1

        constructor(source: Parcel) : super(source) {
            selectedItemId = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedItemId)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}