package ru.practicum.shoppinglist.domain.api.repository

import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

interface ShoppingItemRepository {

    fun observeShoppingItems(
        listId: Long,
    ): Flow<List<ShoppingItem>>

    suspend fun getShoppingItems(
        listId: Long,
    ): List<ShoppingItem>

    suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long

    suspend fun updateShoppingItem(shoppingItem: ShoppingItem)

    suspend fun setPurchased(
        itemId: Long,
        isPurchased: Boolean,
    )

    suspend fun deleteShoppingItem(itemId: Long)

    suspend fun deletePurchasedItems(listId: Long)

    suspend fun updatePositions(
        shoppingItems: List<ShoppingItem>,
    )

    suspend fun findProductSuggestions(
        query: String,
        limit: Int,
    ): List<String>
}
