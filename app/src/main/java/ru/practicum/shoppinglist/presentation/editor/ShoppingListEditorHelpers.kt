package ru.practicum.shoppinglist.presentation.editor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Dialog
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Message
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.State

internal fun MutableStateFlow<State>.showMessage(message: Message) {
    update { state -> state.copy(message = message) }
}

internal fun MutableStateFlow<State>.clearMessage() {
    update { state -> state.copy(message = null) }
}

internal fun MutableStateFlow<State>.changeEditorSort(
    listId: Long,
    sort: ShoppingItemSort,
    selectedSort: MutableStateFlow<ShoppingItemSort>,
    sortStorage: ShoppingItemSortStorage,
) {
    if (sort == selectedSort.value) return

    sortStorage.set(listId, sort)
    selectedSort.value = sort
    update { state -> state.copy(sort = sort) }
}

internal suspend fun ShoppingItemInteractor.persistItem(
    listId: Long,
    form: Dialog.ItemForm,
    name: String,
    quantity: Double?,
): Boolean = if (form.item == null) {
    addShoppingItem(
        listId = listId,
        name = name,
        quantity = quantity,
        unit = form.unit,
    ) != null
} else {
    updateShoppingItem(
        form.item.copy(
            name = name,
            quantity = quantity,
            unit = form.unit,
        ),
    )
}

internal fun String.toQuantityOrNull(): Double? =
    trim()
        .takeIf(String::isNotEmpty)
        ?.replace(',', '.')
        ?.toDoubleOrNull()
        ?.takeIf { quantity -> quantity.isFinite() && quantity > 0 }

internal fun Double?.toEditableQuantity(): String = when {
    this == null -> ""
    this % 1.0 == 0.0 -> toLong().toString()
    else -> toString().replace('.', ',')
}
