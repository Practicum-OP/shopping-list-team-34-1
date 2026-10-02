package ru.practicum.shoppinglist.presentation.editor

import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

internal data class CreateItemDialogState(
    val id: Long? = null,
    val name: String = "",
    val quantity: Int = 1,
    val selectedUnit: MeasurementUnit = MeasurementUnit.PIECE,
    val showNameError: Boolean = false,
    val showQuantityError: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val isEditing: Boolean = false
)

internal data class ListEditorUiState(
    val listName: String = "",
    val items: List<ShoppingItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val createDialog: CreateItemDialogState? = null,
    val listPendingDeletion: ShoppingItem? = null,
    val showClearDialog: Boolean = false,
    val showSortMenu: Boolean = false,
    val sortType: ShoppingItemSort = ShoppingItemSort.MANUAL,
)

internal sealed interface ListEditorIntent {
    data object AddItemClicked : ListEditorIntent
    data class NameChanged(val name: String) : ListEditorIntent
    data class QuantityChanged(val quantity: Int) : ListEditorIntent
    data class UnitChanged(val unit: MeasurementUnit) : ListEditorIntent
    data object ConfirmAddItem : ListEditorIntent
    data class SuggestionSelected(val name: String) : ListEditorIntent
    data object SuggestionsDismissed : ListEditorIntent

    data class ItemEdited(val item: ShoppingItem) : ListEditorIntent
    data class ItemDeleted(val itemId: Long) : ListEditorIntent
    data class ItemEditRequested(val itemId: Long) : ListEditorIntent

    data object DeleteCancelled : ListEditorIntent
    data class ItemToggled(val itemId: Long) : ListEditorIntent
    data class DeleteItemRequested(val itemId: Long) : ListEditorIntent

    data object ClearListClicked : ListEditorIntent
    data object ClearConfirmed : ListEditorIntent
    data object ClearCancelled : ListEditorIntent

    data class SortChanged(val sortType: ShoppingItemSort) : ListEditorIntent
    data object ToggleSortMenu : ListEditorIntent
    data class ItemMoved(
        val itemId: Long,
        val direction: Int,
    ) : ListEditorIntent
}

internal sealed interface ListEditorEffect {
    data object OperationFailed : ListEditorEffect
    data object NavigateBack : ListEditorEffect
}
