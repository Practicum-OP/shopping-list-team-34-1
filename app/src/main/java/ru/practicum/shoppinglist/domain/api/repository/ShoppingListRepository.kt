package ru.practicum.shoppinglist.domain.api.repository

import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

interface ShoppingListRepository {

    fun observeShoppingLists(): Flow<List<ShoppingList>>

    suspend fun getShoppingList(listId: Long): ShoppingList?

    suspend fun createShoppingList(
        name: String,
        iconKey: String,
    ): Long

    suspend fun updateShoppingList(shoppingList: ShoppingList)

    suspend fun deleteShoppingList(listId: Long)

    suspend fun duplicateShoppingList(listId: Long): Long?
}
