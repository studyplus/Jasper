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

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.ActionProvider
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.SubMenu
import android.view.View

internal class MenuItemImpl(
    private val menu: MenuImpl,
    private val group: Int,
    private val id: Int,
    private var title: CharSequence?
) : MenuItem {

    // region text
    private var titleCondensed: CharSequence? = null
    private var contentDescription: CharSequence? = null
    private var tooltipText: CharSequence? = null
    // endregion

    // region icon
    private var iconDrawable: Drawable? = null
    private var iconResId = NO_ICON
    private var iconTintColorStateList: ColorStateList? = null
    private var iconTintMode: PorterDuff.Mode? = null
    private var hasIconTint = false
    private var hasIconTintMode = false
    private var needToApplyIconTint = false
    // endregion

    // region params
    private var clickListener: MenuItem.OnMenuItemClickListener? = null
    private var checkable: Boolean = true
    private var checked: Boolean = false
    private var visible: Boolean = true
    private var enabled: Boolean = true
    // endregion

    override fun getItemId(): Int = id

    override fun getGroupId(): Int = group

    override fun getOrder(): Int {
        throw  UnsupportedOperationException()
    }

    override fun setTitle(title: CharSequence?): MenuItem {
        this.title = title
        return this
    }

    override fun setTitle(@StringRes title: Int): MenuItem {
        return setTitle(menu.context.getString(title))
    }

    override fun getTitle(): CharSequence? {
        return title
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        titleCondensed = title
        return this
    }

    override fun getTitleCondensed(): CharSequence? {
        return if (titleCondensed != null) titleCondensed else title
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        iconResId = NO_ICON
        iconDrawable = icon
        needToApplyIconTint = true

        return this
    }

    override fun setIcon(@DrawableRes iconRes: Int): MenuItem {
        iconDrawable = null
        iconResId = iconRes
        needToApplyIconTint = true

        return this
    }

    override fun getIcon(): Drawable? {
        if (iconDrawable != null) {
            return applyIconTintIfNecessary(iconDrawable)
        }

        if (iconResId != NO_ICON) {
            val icon = menu.context.getDrawable(iconResId)
            iconResId = NO_ICON
            iconDrawable = icon
            return applyIconTintIfNecessary(icon)
        }

        return null
    }

    private fun applyIconTintIfNecessary(icon: Drawable?): Drawable? {
        if (icon == null || !needToApplyIconTint || !(hasIconTint || hasIconTintMode)) {
            return icon
        }

        val drawable = icon.mutate()
        if (hasIconTint) {
            drawable.setTintList(iconTintColorStateList)
        }
        if (hasIconTintMode) {
            drawable.setTintMode(iconTintMode ?: PorterDuff.Mode.SRC_IN)
        }
        needToApplyIconTint = false

        return drawable
    }

    override fun setIconTintList(iconTintList: ColorStateList?): MenuItem {
        this.iconTintColorStateList = iconTintList
        this.hasIconTint = true
        this.needToApplyIconTint = true

        return this
    }

    override fun setIconTintMode(iconTintMode: PorterDuff.Mode?): MenuItem {
        this.iconTintMode = iconTintMode
        this.hasIconTintMode = true
        this.needToApplyIconTint = true

        return this
    }

    override fun getIconTintMode(): PorterDuff.Mode? = iconTintMode

    override fun setIntent(intent: Intent?): MenuItem {
        // Unsupported
        return this
    }

    override fun getIntent(): Intent? = null

    override fun getAlphabeticShortcut(): Char {
        throw UnsupportedOperationException()
    }

    override fun getNumericShortcut(): Char {
        throw UnsupportedOperationException()
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        // Unsupported
        return this
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        // Unsupported
        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        // Unsupported
        return this
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        this.checkable = checkable
        return this
    }

    override fun isCheckable(): Boolean = checkable

    override fun setChecked(checked: Boolean): MenuItem {
        this.checked = checked
        return this
    }

    override fun isChecked(): Boolean = checked

    override fun setVisible(visible: Boolean): MenuItem {
        this.visible = visible
        return this
    }

    override fun isVisible(): Boolean = visible

    override fun setEnabled(enabled: Boolean): MenuItem {
        this.enabled = enabled
        return this
    }

    override fun isEnabled(): Boolean = enabled

    override fun hasSubMenu(): Boolean = false

    override fun getSubMenu(): SubMenu {
        throw UnsupportedOperationException()
    }

    override fun setOnMenuItemClickListener(clickListener: MenuItem.OnMenuItemClickListener): MenuItem {
        this.clickListener = clickListener
        return this
    }

    override fun getMenuInfo(): ContextMenuInfo {
        throw UnsupportedOperationException()
    }

    override fun setShowAsAction(actionEnum: Int) {
        throw UnsupportedOperationException()
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun setActionView(view: View?): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun setActionView(resId: Int): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun getActionView(): View {
        throw UnsupportedOperationException()
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun getActionProvider(): ActionProvider {
        throw UnsupportedOperationException()
    }

    override fun expandActionView(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun collapseActionView(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isActionViewExpanded(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun getContentDescription(): CharSequence? = contentDescription

    override fun setContentDescription(contentDescription: CharSequence?): MenuItem {
        this.contentDescription = contentDescription
        return this
    }

    override fun getTooltipText(): CharSequence? = tooltipText

    override fun setTooltipText(tooltipText: CharSequence?): MenuItem {
        this.tooltipText = tooltipText
        return this
    }

    /**
     * Invokes the item by calling various listeners or callbacks.
     *
     * @return true if the invocation was handled, false otherwise
     */
    fun invoke(): Boolean {
        if (clickListener?.onMenuItemClick(this) == true) {
            return true
        }

        if (menu.invoke(this)) {
            return true
        }

        return false
    }

    companion object {
        private const val NO_ICON = 0
    }
}