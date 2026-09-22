package ru.practicum.shoppinglist.domain.api.interactor

import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

interface ShoppingItemInteractor {

    fun observeShoppingItems(
        listId: Long,
        sort: ShoppingItemSort,
    ): Flow<List<ShoppingItem>>

    suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long?

    suspend fun updateShoppingItem(
        shoppingItem: ShoppingItem,
    ): Boolean

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
        limit: Int = DEFAULT_SUGGESTION_LIMIT,
    ): List<String>

    companion object {
        const val DEFAULT_SUGGESTION_LIMIT = 10
    }
}
