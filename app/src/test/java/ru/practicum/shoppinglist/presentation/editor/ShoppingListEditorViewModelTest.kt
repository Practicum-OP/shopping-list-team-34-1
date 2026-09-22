package ru.practicum.shoppinglist.presentation.editor

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Dialog
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Intent
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Message
import ru.practicum.shoppinglist.presentation.lists.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListEditorViewModelTest {

    @get:Rule
    internal val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saved alphabetical sort is restored and can be changed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val sortStorage = FakeSortStorage(ShoppingItemSort.ALPHABETICAL)
            val itemInteractor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(
                itemInteractor = itemInteractor,
                sortStorage = sortStorage,
            )

            advanceUntilIdle()
            assertEquals(
                ShoppingItemSort.ALPHABETICAL,
                viewModel.state.value.sort,
            )
            assertEquals(
                ShoppingItemSort.ALPHABETICAL,
                itemInteractor.observedSorts.last(),
            )

            viewModel.onIntent(Intent.SortChanged(ShoppingItemSort.MANUAL))
            advanceUntilIdle()

            assertEquals(ShoppingItemSort.MANUAL, sortStorage.sort)
            assertEquals(
                ShoppingItemSort.MANUAL,
                itemInteractor.observedSorts.last(),
            )
        }

    @Test
    fun `blank item name keeps form open and shows validation error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val itemInteractor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(itemInteractor = itemInteractor)
            advanceUntilIdle()

            viewModel.onIntent(Intent.AddItemClicked)
            viewModel.onIntent(Intent.ItemNameChanged("   "))
            viewModel.onIntent(Intent.SaveItemClicked)

            val form = viewModel.state.value.dialog as Dialog.ItemForm
            assertTrue(form.nameError)
            assertEquals(0, itemInteractor.addCalls)
        }

    @Test
    fun `unit is saved when quantity is omitted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val itemInteractor = FakeShoppingItemInteractor()
            val viewModel = createViewModel(itemInteractor = itemInteractor)
            advanceUntilIdle()

            viewModel.onIntent(Intent.AddItemClicked)
            viewModel.onIntent(Intent.ItemNameChanged("Яблоки"))
            viewModel.onIntent(
                Intent.ItemUnitChanged(MeasurementUnit.KILOGRAM),
            )
            viewModel.onIntent(Intent.SaveItemClicked)
            advanceUntilIdle()

            assertEquals(1, itemInteractor.addCalls)
            assertNull(itemInteractor.lastQuantity)
            assertEquals(
                MeasurementUnit.KILOGRAM,
                itemInteractor.lastUnit,
            )
            assertNull(viewModel.state.value.dialog)
        }

    @Test
    fun `rename saves trimmed name and selected icon`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val listInteractor = FakeShoppingListInteractor()
            val viewModel = createViewModel(listInteractor = listInteractor)
            advanceUntilIdle()

            viewModel.onIntent(Intent.RenameListClicked)
            viewModel.onIntent(Intent.ListNameChanged("  Дача  "))
            viewModel.onIntent(Intent.ListIconChanged("home"))
            viewModel.onIntent(Intent.SaveListNameClicked)
            advanceUntilIdle()

            assertEquals("Дача", listInteractor.updatedList?.name)
            assertEquals("home", listInteractor.updatedList?.iconKey)
            assertFalse(viewModel.state.value.isLoading)
            assertNull(viewModel.state.value.dialog)
        }

    @Test
    fun `missing list exposes retryable error state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                listInteractor = FakeShoppingListInteractor(listResult = null),
            )

            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
            assertEquals(Message.LIST_NOT_FOUND, viewModel.state.value.loadError)
            assertNull(viewModel.state.value.message)
        }

    @Test
    fun `retry clears a transient load error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val listInteractor = FakeShoppingListInteractor().apply {
                failure = IllegalStateException("temporary")
            }
            val viewModel = createViewModel(listInteractor = listInteractor)
            advanceUntilIdle()
            assertEquals(Message.LOAD_FAILED, viewModel.state.value.loadError)

            listInteractor.failure = null
            viewModel.onIntent(Intent.RetryClicked)
            advanceUntilIdle()

            assertNull(viewModel.state.value.loadError)
            assertEquals("Продукты", viewModel.state.value.listName)
        }

    @Test
    fun `confirmed list deletion blocks further editor actions`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val deletionGate = CompletableDeferred<Unit>()
            val listInteractor = FakeShoppingListInteractor().apply {
                deleteGate = deletionGate
            }
            val viewModel = createViewModel(listInteractor = listInteractor)
            advanceUntilIdle()

            viewModel.onIntent(Intent.DeleteListClicked)
            viewModel.onIntent(Intent.ConfirmDeleteListClicked)

            assertTrue(viewModel.state.value.isDeletingList)
            assertNull(viewModel.state.value.dialog)

            viewModel.onIntent(Intent.AddItemClicked)
            assertNull(viewModel.state.value.dialog)

            deletionGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(listOf(LIST_ID), listInteractor.deletedListIds)
        }

    private fun createViewModel(
        listInteractor: FakeShoppingListInteractor =
            FakeShoppingListInteractor(),
        itemInteractor: FakeShoppingItemInteractor =
            FakeShoppingItemInteractor(),
        sortStorage: FakeSortStorage = FakeSortStorage(),
    ) = ShoppingListEditorViewModel(
        listId = LIST_ID,
        shoppingListInteractor = listInteractor,
        shoppingItemInteractor = itemInteractor,
        sortStorage = sortStorage,
    )

    private companion object {
        const val LIST_ID = 7L
    }
}

private class FakeSortStorage(
    initialSort: ShoppingItemSort = ShoppingItemSort.MANUAL,
) : ShoppingItemSortStorage {
    var sort = initialSort

    override fun get(listId: Long): ShoppingItemSort = sort

    override fun set(listId: Long, sort: ShoppingItemSort) {
        this.sort = sort
    }

    override fun remove(listId: Long) {
        sort = ShoppingItemSort.MANUAL
    }
}

private class FakeShoppingListInteractor(
    var listResult: ShoppingList? = ShoppingList(
        id = 7,
        name = "Продукты",
        iconKey = "shopping_cart",
        createdAt = 1,
    ),
) : ShoppingListInteractor {
    var failure: Throwable? = null
    var updatedList: ShoppingList? = null
    var deleteGate: CompletableDeferred<Unit>? = null
    val deletedListIds = mutableListOf<Long>()

    override fun observeShoppingLists(): Flow<List<ShoppingList>> =
        MutableStateFlow(listOfNotNull(listResult))

    override suspend fun getShoppingList(listId: Long): ShoppingList? {
        failure?.let { throw it }
        return listResult
    }

    override suspend fun createShoppingList(
        name: String,
        iconKey: String,
    ): Long = 1

    override suspend fun updateShoppingList(
        shoppingList: ShoppingList,
    ): Boolean {
        updatedList = shoppingList
        return true
    }

    override suspend fun deleteShoppingList(listId: Long) {
        deleteGate?.await()
        deletedListIds += listId
    }

    override suspend fun duplicateShoppingList(listId: Long): Long? = null
}

private class FakeShoppingItemInteractor : ShoppingItemInteractor {
    private val items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val observedSorts = mutableListOf<ShoppingItemSort>()
    var addCalls = 0
    var lastQuantity: Double? = null
    var lastUnit: MeasurementUnit? = null

    override fun observeShoppingItems(
        listId: Long,
        sort: ShoppingItemSort,
    ): Flow<List<ShoppingItem>> {
        observedSorts += sort
        return items
    }

    override suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long {
        addCalls += 1
        lastQuantity = quantity
        lastUnit = unit
        return 42
    }

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
    ) = Unit

    override suspend fun findProductSuggestions(
        query: String,
        limit: Int,
    ): List<String> = emptyList()
}
