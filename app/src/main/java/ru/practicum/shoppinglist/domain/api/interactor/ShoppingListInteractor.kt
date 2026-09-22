package ru.practicum.shoppinglist.domain.api.interactor

import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

interface ShoppingListInteractor {

    fun observeShoppingLists(): Flow<List<ShoppingList>>

    suspend fun getShoppingList(listId: Long): ShoppingList?

    suspend fun createShoppingList(
        name: String,
        iconKey: String,
    ): Long?

    suspend fun updateShoppingList(
        shoppingList: ShoppingList,
    ): Boolean

    suspend fun deleteShoppingList(listId: Long)

    suspend fun duplicateShoppingList(listId: Long): Long?
}
