package ru.practicum.shoppinglist.presentation.lists

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
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListsViewModelTest {

    @get:Rule
    internal val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `lists flow produces filled state`() = runTest(mainDispatcherRule.testDispatcher) {
        val list = shoppingList(id = 7, name = "На неделю")
        val interactor = FakeShoppingListInteractor(listOf(list))
        val viewModel = ShoppingListsViewModel(interactor)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(list), viewModel.state.value.lists)
    }

    @Test
    fun `blank name is rejected without interactor call`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingListInteractor()
            val viewModel = ShoppingListsViewModel(interactor)
            advanceUntilIdle()

            viewModel.onAction(ShoppingListsAction.CreateListClicked)
            viewModel.onAction(ShoppingListsAction.CreateNameChanged("   "))
            viewModel.onAction(ShoppingListsAction.CreateConfirmed)

            assertTrue(viewModel.state.value.createDialog?.showNameError == true)
            assertEquals(0, interactor.createCalls)
        }

    @Test
    fun `valid list is created with trimmed name and selected icon`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingListInteractor()
            val viewModel = ShoppingListsViewModel(interactor)
            advanceUntilIdle()

            viewModel.onAction(ShoppingListsAction.CreateListClicked)
            viewModel.onAction(ShoppingListsAction.CreateNameChanged("  Дача  "))
            viewModel.onAction(ShoppingListsAction.CreateIconSelected("home"))
            viewModel.onAction(ShoppingListsAction.CreateConfirmed)
            advanceUntilIdle()

            assertEquals("Дача", interactor.lastCreatedName)
            assertEquals("home", interactor.lastCreatedIconKey)
            assertNull(viewModel.state.value.createDialog)
        }

    @Test
    fun `list is renamed with trimmed name and selected icon`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingListInteractor()
            val viewModel = ShoppingListsViewModel(interactor)
            val list = shoppingList(id = 9, name = "Old name")
            advanceUntilIdle()

            viewModel.onAction(ShoppingListsAction.RenameListClicked(list))
            viewModel.onAction(ShoppingListsAction.RenameNameChanged("  Weekend  "))
            viewModel.onAction(ShoppingListsAction.RenameIconSelected("home"))
            viewModel.onAction(ShoppingListsAction.RenameConfirmed)
            advanceUntilIdle()

            assertEquals("Weekend", interactor.updatedList?.name)
            assertEquals("home", interactor.updatedList?.iconKey)
            assertNull(viewModel.state.value.renameDialog)
        }

    @Test
    fun `delete cancellation leaves data unchanged and confirmation deletes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val interactor = FakeShoppingListInteractor()
            val viewModel = ShoppingListsViewModel(interactor)
            val list = shoppingList(id = 11, name = "Удалить")
            advanceUntilIdle()

            viewModel.onAction(ShoppingListsAction.DeleteListClicked(list))
            viewModel.onAction(ShoppingListsAction.DeleteDialogDismissed)
            assertTrue(interactor.deletedIds.isEmpty())

            viewModel.onAction(ShoppingListsAction.DeleteListClicked(list))
            viewModel.onAction(ShoppingListsAction.DeleteConfirmed)
            advanceUntilIdle()

            assertEquals(listOf(11L), interactor.deletedIds)
            assertNull(viewModel.state.value.listPendingDeletion)
        }

    private fun shoppingList(id: Long, name: String) = ShoppingList(
        id = id,
        name = name,
        iconKey = ShoppingListIconRegistry.DEFAULT_KEY,
        createdAt = 1L,
    )
}

private class FakeShoppingListInteractor(
    initialLists: List<ShoppingList> = emptyList(),
) : ShoppingListInteractor {
    private val lists = MutableStateFlow(initialLists)

    var createCalls = 0
    var lastCreatedName: String? = null
    var lastCreatedIconKey: String? = null
    var updatedList: ShoppingList? = null
    val deletedIds = mutableListOf<Long>()

    override fun observeShoppingLists(): Flow<List<ShoppingList>> = lists

    override suspend fun getShoppingList(listId: Long): ShoppingList? =
        lists.value.firstOrNull { it.id == listId }

    override suspend fun createShoppingList(name: String, iconKey: String): Long {
        createCalls += 1
        lastCreatedName = name
        lastCreatedIconKey = iconKey
        return 42L
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList): Boolean {
        updatedList = shoppingList
        return true
    }

    override suspend fun deleteShoppingList(listId: Long) {
        deletedIds += listId
    }

    override suspend fun duplicateShoppingList(listId: Long): Long? = null
}
