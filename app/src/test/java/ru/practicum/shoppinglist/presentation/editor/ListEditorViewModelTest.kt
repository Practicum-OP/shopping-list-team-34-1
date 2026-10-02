package ru.practicum.shoppinglist.presentation.editor

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.presentation.lists.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class ListEditorViewModelTest {

    @get:Rule
    internal val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `suggestions are loaded after debounce and selected name is applied`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingItemInteractor().apply {
                suggestions = listOf("Молоко", "Молочный шоколад")
            }
            val viewModel = createViewModel(interactor)
            runCurrent()

            viewModel.onIntent(ListEditorIntent.AddItemClicked)
            viewModel.onIntent(ListEditorIntent.NameChanged("Мол"))
            advanceTimeBy(SUGGESTIONS_DEBOUNCE_TEST_MILLIS - 1)
            assertTrue(interactor.suggestionQueries.isEmpty())

            advanceTimeBy(1)
            runCurrent()

            assertEquals(listOf("Мол"), interactor.suggestionQueries)
            assertEquals(
                listOf("Молоко", "Молочный шоколад"),
                viewModel.state.value.createDialog?.suggestions,
            )

            viewModel.onIntent(
                ListEditorIntent.SuggestionSelected("Молоко"),
            )

            assertEquals("Молоко", viewModel.state.value.createDialog?.name)
            assertTrue(
                viewModel.state.value.createDialog?.suggestions?.isEmpty() == true,
            )
        }

    @Test
    fun `manual move persists full normalized order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(interactor)
            runCurrent()

            viewModel.onIntent(
                ListEditorIntent.ItemMoved(
                    itemId = FIRST_ITEM_ID,
                    direction = 1,
                ),
            )
            advanceUntilIdle()

            val savedItems = interactor.updatedOrders.single()
            assertEquals(
                listOf(SECOND_ITEM_ID, FIRST_ITEM_ID, THIRD_ITEM_ID),
                savedItems.map(ShoppingItem::id),
            )
            assertEquals(listOf(0, 1, 2), savedItems.map(ShoppingItem::position))
        }

    @Test
    fun `rapid moves persist the latest order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(interactor)
            runCurrent()

            repeat(2) {
                viewModel.onIntent(
                    ListEditorIntent.ItemMoved(
                        itemId = FIRST_ITEM_ID,
                        direction = 1,
                    ),
                )
            }
            advanceUntilIdle()

            assertEquals(
                listOf(SECOND_ITEM_ID, THIRD_ITEM_ID, FIRST_ITEM_ID),
                interactor.updatedOrders.last().map(ShoppingItem::id),
            )
        }

    @Test
    fun `room item update during position save is preserved after success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val saveStarted = CompletableDeferred<Unit>()
            val allowSave = CompletableDeferred<Unit>()
            val interactor = FakeShoppingItemInteractor().apply {
                positionUpdateStarted = saveStarted
                positionUpdateGate = allowSave
            }
            val viewModel = createViewModel(interactor)
            runCurrent()

            viewModel.onIntent(
                ListEditorIntent.ItemMoved(
                    itemId = FIRST_ITEM_ID,
                    direction = 1,
                ),
            )
            runCurrent()
            assertTrue(saveStarted.isCompleted)

            interactor.manualItems.value = interactor.manualItems.value.map { item ->
                if (item.id == FIRST_ITEM_ID) {
                    item.copy(name = "Updated apple", isPurchased = true)
                } else {
                    item
                }
            }
            runCurrent()

            assertEquals(
                listOf(SECOND_ITEM_ID, FIRST_ITEM_ID, THIRD_ITEM_ID),
                viewModel.state.value.items.map(ShoppingItem::id),
            )
            assertEquals(
                "Updated apple" to true,
                viewModel.state.value.items
                    .first { it.id == FIRST_ITEM_ID }
                    .let { it.name to it.isPurchased },
            )

            allowSave.complete(Unit)
            advanceUntilIdle()

            assertEquals(
                "Updated apple" to true,
                viewModel.state.value.items
                    .first { it.id == FIRST_ITEM_ID }
                    .let { it.name to it.isPurchased },
            )
        }

    @Test
    fun `failed position save rolls back order without losing latest room fields`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val saveStarted = CompletableDeferred<Unit>()
            val allowSave = CompletableDeferred<Unit>()
            val interactor = FakeShoppingItemInteractor().apply {
                positionUpdateStarted = saveStarted
                positionUpdateGate = allowSave
                positionUpdateFailure = IllegalStateException("Concurrent list change")
            }
            val viewModel = createViewModel(interactor)
            runCurrent()

            viewModel.onIntent(
                ListEditorIntent.ItemMoved(
                    itemId = FIRST_ITEM_ID,
                    direction = 1,
                ),
            )
            runCurrent()
            assertTrue(saveStarted.isCompleted)

            interactor.manualItems.value = interactor.manualItems.value.map { item ->
                if (item.id == THIRD_ITEM_ID) item.copy(isPurchased = true) else item
            }
            runCurrent()
            allowSave.complete(Unit)
            advanceUntilIdle()

            assertEquals(
                listOf(FIRST_ITEM_ID, SECOND_ITEM_ID, THIRD_ITEM_ID),
                viewModel.state.value.items.map(ShoppingItem::id),
            )
            assertTrue(
                viewModel.state.value.items
                    .first { it.id == THIRD_ITEM_ID }
                    .isPurchased,
            )
        }

    @Test
    fun `alphabetical mode ignores moves and cancels manual observer`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(interactor)
            runCurrent()

            viewModel.onIntent(
                ListEditorIntent.SortChanged(ShoppingItemSort.ALPHABETICAL),
            )
            runCurrent()
            val alphabeticalItems = listOf(
                item(FIRST_ITEM_ID, "Апельсин", 0),
                item(SECOND_ITEM_ID, "Банан", 1),
                item(THIRD_ITEM_ID, "Яблоко", 2),
            )
            interactor.alphabeticalItems.value = alphabeticalItems
            runCurrent()

            interactor.manualItems.value = listOf(
                item(THIRD_ITEM_ID, "Яблоко", 0),
            )
            runCurrent()

            assertEquals(
                alphabeticalItems,
                viewModel.state.value.items,
            )
            viewModel.onIntent(
                ListEditorIntent.ItemMoved(
                    itemId = FIRST_ITEM_ID,
                    direction = 1,
                ),
            )
            advanceUntilIdle()

            assertTrue(interactor.updatedOrders.isEmpty())
            assertEquals(
                listOf(
                    ShoppingItemSort.MANUAL,
                    ShoppingItemSort.ALPHABETICAL,
                ),
                interactor.observedSorts,
            )
        }

    private fun createViewModel(
        interactor: ShoppingItemInteractor,
    ): ListEditorViewModel = ListEditorViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                "listId" to LIST_ID,
                "listName" to "Продукты",
            ),
        ),
        interactor = interactor,
    )

    private companion object {
        const val LIST_ID = 7L
        const val FIRST_ITEM_ID = 1L
        const val SECOND_ITEM_ID = 2L
        const val THIRD_ITEM_ID = 3L
        const val SUGGESTIONS_DEBOUNCE_TEST_MILLIS = 300L
    }
}

private class FakeShoppingItemInteractor : ShoppingItemInteractor {
    val manualItems = MutableStateFlow(
        listOf(
            item(id = 1, name = "Яблоко", position = 0),
            item(id = 2, name = "Банан", position = 1),
            item(id = 3, name = "Апельсин", position = 2),
        ),
    )
    val alphabeticalItems = MutableStateFlow(manualItems.value.reversed())
    val observedSorts = mutableListOf<ShoppingItemSort>()
    val updatedOrders = mutableListOf<List<ShoppingItem>>()
    val suggestionQueries = mutableListOf<String>()
    var suggestions: List<String> = emptyList()
    var positionUpdateStarted: CompletableDeferred<Unit>? = null
    var positionUpdateGate: CompletableDeferred<Unit>? = null
    var positionUpdateFailure: Throwable? = null

    override fun observeShoppingItems(
        listId: Long,
        sort: ShoppingItemSort,
    ): Flow<List<ShoppingItem>> {
        observedSorts += sort
        return when (sort) {
            ShoppingItemSort.MANUAL -> manualItems
            ShoppingItemSort.ALPHABETICAL -> alphabeticalItems
        }
    }

    override suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long = 10

    override suspend fun updateShoppingItem(
        shoppingItem: ShoppingItem,
    ): Boolean = true

    override suspend fun setPurchased(
        itemId: Long,
        isPurchased: Boolean,
    ) = Unit

    override suspend fun deleteShoppingItem(itemId: Long) = Unit

    override suspend fun deletePurchasedItems(listId: Long) = Unit

    override suspend fun updatePositions(
        shoppingItems: List<ShoppingItem>,
    ) {
        updatedOrders += shoppingItems
        positionUpdateStarted?.complete(Unit)
        positionUpdateGate?.await()
        positionUpdateFailure?.let { throw it }

        val latestItemsById = manualItems.value.associateBy(ShoppingItem::id)
        manualItems.value = shoppingItems.mapNotNull { positionedItem ->
            latestItemsById[positionedItem.id]?.copy(position = positionedItem.position)
        }
    }

    override suspend fun findProductSuggestions(
        query: String,
        limit: Int,
    ): List<String> {
        suggestionQueries += query
        return suggestions.take(limit)
    }
}

private fun item(
    id: Long,
    name: String,
    position: Int,
) = ShoppingItem(
    id = id,
    shoppingListId = 7,
    name = name,
    quantity = 1.0,
    unit = MeasurementUnit.PIECE,
    isPurchased = false,
    position = position,
)
