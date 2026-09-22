package ru.practicum.shoppinglist.presentation.editor

import android.content.SharedPreferences
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

internal interface ShoppingItemSortStorage {
    fun get(listId: Long): ShoppingItemSort

    fun set(
        listId: Long,
        sort: ShoppingItemSort,
    )

    fun remove(listId: Long)
}

internal class SharedPreferencesShoppingItemSortStorage(
    private val preferences: SharedPreferences,
) : ShoppingItemSortStorage {

    override fun get(listId: Long): ShoppingItemSort {
        val value = preferences.getString(key(listId), null)
        return ShoppingItemSort.entries.firstOrNull { sort ->
            sort.name == value
        } ?: ShoppingItemSort.MANUAL
    }

    override fun set(
        listId: Long,
        sort: ShoppingItemSort,
    ) {
        preferences.edit()
            .putString(key(listId), sort.name)
            .apply()
    }

    override fun remove(listId: Long) {
        preferences.edit()
            .remove(key(listId))
            .apply()
    }

    private fun key(listId: Long): String = "shopping_item_sort_$listId"

    companion object {
        const val PREFERENCES_NAME = "editor_preferences"
    }
}
