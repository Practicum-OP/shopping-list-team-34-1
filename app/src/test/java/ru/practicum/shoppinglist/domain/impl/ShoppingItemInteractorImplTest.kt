package ru.practicum.shoppinglist.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.repository.ShoppingItemRepository

class ShoppingItemInteractorImplTest {

    @Test
    fun `updatePositions assigns contiguous positions in supplied order`() = runTest {
        val repository = FakeShoppingItemRepository()
        val interactor = ShoppingItemInteractorImpl(repository)

        interactor.updatePositions(
            listOf(
                item(id = 3, position = 2),
                item(id = 1, position = 0),
                item(id = 2, position = 1),
            ),
        )

        assertEquals(listOf(3L, 1L, 2L), repository.updatedOrder.map(ShoppingItem::id))
        assertEquals(listOf(0, 1, 2), repository.updatedOrder.map(ShoppingItem::position))
    }

    @Test
    fun `updatePositions rejects incomplete identity data`() = runTest {
        val repository = FakeShoppingItemRepository()
        val interactor = ShoppingItemInteractorImpl(repository)

        interactor.updatePositions(
            listOf(
                item(id = 1, listId = 4),
                item(id = 2, listId = 5),
            ),
        )
        interactor.updatePositions(listOf(item(id = 1), item(id = 1)))

        assertTrue(repository.updatedOrder.isEmpty())
    }

    @Test
    fun `suggestions trim query and clamp excessive limit`() = runTest {
        val repository = FakeShoppingItemRepository()
        val interactor = ShoppingItemInteractorImpl(repository)

        interactor.findProductSuggestions("  Мо  ", limit = 100)

        assertEquals("Мо", repository.suggestionQuery)
        assertEquals(20, repository.suggestionLimit)
    }

    private fun item(
        id: Long,
        listId: Long = 4,
        position: Int = 0,
    ) = ShoppingItem(
        id = id,
        shoppingListId = listId,
        name = "Товар $id",
        quantity = null,
        unit = null,
        isPurchased = false,
        position = position,
    )
}

private class FakeShoppingItemRepository : ShoppingItemRepository {
    var updatedOrder: List<ShoppingItem> = emptyList()
    var suggestionQuery: String? = null
    var suggestionLimit: Int? = null

    override fun observeShoppingItems(listId: Long): Flow<List<ShoppingItem>> = flowOf(emptyList())

    override suspend fun getShoppingItems(listId: Long): List<ShoppingItem> = emptyList()

    override suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long = 1L

    override suspend fun updateShoppingItem(shoppingItem: ShoppingItem) = Unit

    override suspend fun setPurchased(itemId: Long, isPurchased: Boolean) = Unit

    override suspend fun deleteShoppingItem(itemId: Long) = Unit

    override suspend fun deletePurchasedItems(listId: Long) = Unit

    override suspend fun updatePositions(shoppingItems: List<ShoppingItem>) {
        updatedOrder = shoppingItems
    }

    override suspend fun findProductSuggestions(query: String, limit: Int): List<String> {
        suggestionQuery = query
        suggestionLimit = limit
        return emptyList()
    }
}
