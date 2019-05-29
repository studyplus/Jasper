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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu

class MenuImpl(
    val context: Context,
    val invoke: (item: MenuItem) -> Boolean
) : Menu {

    private val items = mutableListOf<MenuItemImpl>()

    override fun add(title: CharSequence?): MenuItem =
        add(0, 0, title)

    override fun add(titleRes: Int): MenuItem =
        add(0, 0, context.resources.getString(titleRes))

    override fun add(groupId: Int, itemId: Int, order: Int, title: CharSequence?): MenuItem =
        add(groupId, itemId, title)

    override fun add(groupId: Int, itemId: Int, order: Int, titleRes: Int): MenuItem =
        add(groupId, itemId, context.resources.getString(titleRes))

    private fun add(group: Int, id: Int, title: CharSequence?): MenuItem {
        val item = createNewMenuItem(group, id, title)
        items.add(item)

        return item
    }

    override fun addSubMenu(title: CharSequence?): SubMenu =
        throw UnsupportedOperationException()

    override fun addSubMenu(titleRes: Int): SubMenu =
        throw UnsupportedOperationException()

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, title: CharSequence?): SubMenu =
        throw UnsupportedOperationException()

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, titleRes: Int): SubMenu =
        throw UnsupportedOperationException()

    override fun addIntentOptions(
        groupId: Int,
        itemId: Int,
        order: Int,
        caller: ComponentName?,
        specifics: Array<out Intent>?,
        intent: Intent?,
        flags: Int,
        outSpecificItems: Array<out MenuItem>?
    ): Int {
        throw UnsupportedOperationException()
    }

    override fun removeItem(id: Int) {
        items.firstOrNull { it.itemId == id }?.let { items.remove(it) }
    }

    override fun removeGroup(groupId: Int) {
        items.removeAll { it.groupId == groupId }
    }

    override fun clear() {
        items.clear()
    }

    override fun setGroupCheckable(group: Int, checkable: Boolean, exclusive: Boolean) {
        items.filter { it.groupId == group }.forEach { it.isCheckable = checkable }
    }

    override fun setGroupVisible(group: Int, visible: Boolean) {
        items.filter { it.groupId == group }.forEach { it.isVisible = visible }
    }

    override fun setGroupEnabled(group: Int, enabled: Boolean) {
        items.filter { it.groupId == group }.forEach { it.isEnabled = enabled }
    }

    override fun hasVisibleItems(): Boolean {
        return items.firstOrNull { it.isVisible } != null
    }

    override fun findItem(id: Int): MenuItem? {
        return items.firstOrNull { it.itemId == id }
    }

    override fun size(): Int {
        return items.size
    }

    override fun getItem(index: Int): MenuItem {
        return items[index]
    }

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun performShortcut(keyCode: Int, event: KeyEvent?, flags: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isShortcutKey(keyCode: Int, event: KeyEvent?): Boolean {
        throw UnsupportedOperationException()
    }

    fun performIdentifierAction(id: Int): Boolean {
        return performIdentifierAction(id, 0)
    }

    override fun performIdentifierAction(id: Int, flags: Int): Boolean {
        return performItemAction(findItem(id))
    }

    private fun performItemAction(menuItem: MenuItem?): Boolean {
        val itemImpl = menuItem as? MenuItemImpl
        if (itemImpl == null || !itemImpl.isEnabled) {
            return false
        }

        // setItemData item's isChecked field
        items.forEach { it.isChecked = it.itemId == menuItem.itemId }

        return itemImpl.invoke()
    }

    override fun setQwertyMode(isQwerty: Boolean) {
        throw UnsupportedOperationException()
    }

    private fun createNewMenuItem(group: Int, id: Int, title: CharSequence?): MenuItemImpl {
        return MenuItemImpl(this, group, id, title)
    }
}