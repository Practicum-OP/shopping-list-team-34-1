package ru.practicum.shoppinglist.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.domain.api.repository.ShoppingItemRepository

class ShoppingItemInteractorImplTest {

    private val repository = FakeShoppingItemRepository()
    private val interactor = ShoppingItemInteractorImpl(repository)

    @Test
    fun `alphabetical sort ignores case and keeps position for equal names`() =
        runTest {
            repository.items.value = listOf(
                item(id = 1, name = "банан", position = 2),
                item(id = 2, name = "Арбуз", position = 0),
                item(id = 3, name = "Банан", position = 1),
            )

            val result = interactor.observeShoppingItems(
                listId = LIST_ID,
                sort = ShoppingItemSort.ALPHABETICAL,
            ).first()

            assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
        }

    @Test
    fun `add trims a valid name before saving`() = runTest {
        val result = interactor.addShoppingItem(
            listId = LIST_ID,
            name = "  Молоко  ",
            quantity = 2.0,
            unit = MeasurementUnit.LITER,
        )

        assertEquals(NEW_ITEM_ID, result)
        assertEquals("Молоко", repository.lastAddedName)
        assertEquals(2.0, repository.lastAddedQuantity)
        assertEquals(MeasurementUnit.LITER, repository.lastAddedUnit)
    }

    @Test
    fun `unit can be saved without quantity`() = runTest {
        interactor.addShoppingItem(
            listId = LIST_ID,
            name = "Яблоки",
            quantity = null,
            unit = MeasurementUnit.KILOGRAM,
        )

        assertNull(repository.lastAddedQuantity)
        assertEquals(MeasurementUnit.KILOGRAM, repository.lastAddedUnit)
    }

    @Test
    fun `add rejects blank name and non-positive quantity`() = runTest {
        assertNull(
            interactor.addShoppingItem(
                listId = LIST_ID,
                name = "   ",
                quantity = null,
                unit = null,
            ),
        )
        assertNull(
            interactor.addShoppingItem(
                listId = LIST_ID,
                name = "Хлеб",
                quantity = 0.0,
                unit = MeasurementUnit.PIECE,
            ),
        )
        assertEquals(0, repository.addCalls)
    }

    private fun item(
        id: Long,
        name: String,
        position: Int,
    ) = ShoppingItem(
        id = id,
        shoppingListId = LIST_ID,
        name = name,
        quantity = null,
        unit = null,
        isPurchased = false,
        position = position,
    )

    private class FakeShoppingItemRepository : ShoppingItemRepository {
        val items = MutableStateFlow<List<ShoppingItem>>(emptyList())
        var addCalls = 0
        var lastAddedName: String? = null
        var lastAddedQuantity: Double? = null
        var lastAddedUnit: MeasurementUnit? = null

        override fun observeShoppingItems(listId: Long): Flow<List<ShoppingItem>> =
            items

        override suspend fun getShoppingItems(listId: Long): List<ShoppingItem> =
            items.value

        override suspend fun addShoppingItem(
            listId: Long,
            name: String,
            quantity: Double?,
            unit: MeasurementUnit?,
        ): Long {
            addCalls += 1
            lastAddedName = name
            lastAddedQuantity = quantity
            lastAddedUnit = unit
            return NEW_ITEM_ID
        }

        override suspend fun updateShoppingItem(shoppingItem: ShoppingItem) = Unit

        override suspend fun setPurchased(
            itemId: Long,
            isPurchased: Boolean,
        ) = Unit

        override suspend fun deleteShoppingItem(itemId: Long) = Unit

        override suspend fun deletePurchasedItems(listId: Long) = Unit

        override suspend fun updatePositions(
            shoppingItems: List<ShoppingItem>,
        ) = Unit

        override suspend fun findProductSuggestions(
            query: String,
            limit: Int,
        ): List<String> = emptyList()
    }

    private companion object {
        const val LIST_ID = 7L
        const val NEW_ITEM_ID = 42L
    }
}
