package ru.practicum.shoppinglist.presentation.editor

import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

internal object ShoppingListEditorContract {

    data class State(
        val listName: String = "",
        val items: List<ShoppingItem> = emptyList(),
        val sort: ShoppingItemSort = ShoppingItemSort.MANUAL,
        val isLoading: Boolean = true,
        val loadError: Message? = null,
        val dialog: Dialog? = null,
        val message: Message? = null,
    )

    sealed interface Dialog {

        data class ItemForm(
            val item: ShoppingItem? = null,
            val name: String = "",
            val quantity: String = "",
            val unit: MeasurementUnit? = null,
            val nameError: Boolean = false,
            val quantityError: Boolean = false,
            val isSaving: Boolean = false,
        ) : Dialog

        data class RenameList(
            val name: String,
            val iconKey: String,
            val hasError: Boolean = false,
            val isSaving: Boolean = false,
        ) : Dialog

        data class DeleteItem(
            val item: ShoppingItem,
        ) : Dialog

        data object DeleteList : Dialog

        data object ClearPurchased : Dialog
    }

    sealed interface Intent {
        data object AddItemClicked : Intent
        data class EditItemClicked(val item: ShoppingItem) : Intent
        data class DeleteItemClicked(val item: ShoppingItem) : Intent
        data class PurchasedChanged(
            val itemId: Long,
            val isPurchased: Boolean,
        ) : Intent

        data class SortChanged(val sort: ShoppingItemSort) : Intent
        data object RenameListClicked : Intent
        data object DeleteListClicked : Intent
        data object ClearPurchasedClicked : Intent
        data class ItemNameChanged(val value: String) : Intent
        data class ItemQuantityChanged(val value: String) : Intent
        data class ItemUnitChanged(val value: MeasurementUnit?) : Intent
        data class ListNameChanged(val value: String) : Intent
        data class ListIconChanged(val iconKey: String) : Intent
        data object SaveItemClicked : Intent
        data object SaveListNameClicked : Intent
        data object ConfirmDeleteItemClicked : Intent
        data object ConfirmDeleteListClicked : Intent
        data object ConfirmClearPurchasedClicked : Intent
        data object DialogDismissed : Intent
        data object MessageShown : Intent
        data object RetryClicked : Intent
    }

    sealed interface Effect {
        data object NavigateBack : Effect
    }

    enum class Message {
        LIST_NOT_FOUND,
        LOAD_FAILED,
        SAVE_FAILED,
    }
}
