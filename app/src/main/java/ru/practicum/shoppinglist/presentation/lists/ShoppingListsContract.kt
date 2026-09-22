package ru.practicum.shoppinglist.presentation.lists

import ru.practicum.shoppinglist.domain.api.model.ShoppingList

internal data class CreateListDialogState(
    val name: String = "",
    val iconKey: String = ShoppingListIconRegistry.default.key,
    val showNameError: Boolean = false,
)

internal data class ShoppingListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val createDialog: CreateListDialogState? = null,
    val listPendingDeletion: ShoppingList? = null,
    val isSubmitting: Boolean = false,
)

internal sealed interface ShoppingListsAction {
    data object CreateListClicked : ShoppingListsAction
    data object CreateDialogDismissed : ShoppingListsAction
    data class CreateNameChanged(val name: String) : ShoppingListsAction
    data class CreateIconSelected(val iconKey: String) : ShoppingListsAction
    data object CreateConfirmed : ShoppingListsAction
    data class ListClicked(val listId: Long) : ShoppingListsAction
    data class DeleteListClicked(val shoppingList: ShoppingList) : ShoppingListsAction
    data object DeleteDialogDismissed : ShoppingListsAction
    data object DeleteConfirmed : ShoppingListsAction
}

internal sealed interface ShoppingListsEffect {
    data class OpenList(val listId: Long) : ShoppingListsEffect
    data object OperationFailed : ShoppingListsEffect
}
