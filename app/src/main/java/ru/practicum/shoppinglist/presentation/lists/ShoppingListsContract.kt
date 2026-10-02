package ru.practicum.shoppinglist.presentation.lists

import ru.practicum.shoppinglist.domain.api.model.ShoppingList

internal data class CreateListDialogState(
    val name: String = "",
    val iconKey: String = ShoppingListIconRegistry.default.key,
    val showNameError: Boolean = false,
)

internal data class RenameListDialogState(
    val shoppingList: ShoppingList,
    val name: String = shoppingList.name,
    val iconKey: String = ShoppingListIconRegistry.resolve(shoppingList.iconKey).key,
    val showNameError: Boolean = false,
)

internal data class ShoppingListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val createDialog: CreateListDialogState? = null,
    val renameDialog: RenameListDialogState? = null,
    val listPendingDeletion: ShoppingList? = null,
    val isSubmitting: Boolean = false,
    val isLoggingOut: Boolean = false,
)

internal sealed interface ShoppingListsAction {
    data object CreateListClicked : ShoppingListsAction
    data object CreateDialogDismissed : ShoppingListsAction
    data class CreateNameChanged(val name: String) : ShoppingListsAction
    data class CreateIconSelected(val iconKey: String) : ShoppingListsAction
    data object CreateConfirmed : ShoppingListsAction
    data class RenameListClicked(val shoppingList: ShoppingList) : ShoppingListsAction
    data object RenameDialogDismissed : ShoppingListsAction
    data class RenameNameChanged(val name: String) : ShoppingListsAction
    data class RenameIconSelected(val iconKey: String) : ShoppingListsAction
    data object RenameConfirmed : ShoppingListsAction
    data class DeleteListClicked(val shoppingList: ShoppingList) : ShoppingListsAction
    data object DeleteDialogDismissed : ShoppingListsAction
    data object DeleteConfirmed : ShoppingListsAction
    data object LogoutClicked : ShoppingListsAction
}

internal sealed interface ShoppingListsEffect {
    data object OperationFailed : ShoppingListsEffect
    data object NavigateToList : ShoppingListsEffect
    data object NavigateToLogin : ShoppingListsEffect
}
