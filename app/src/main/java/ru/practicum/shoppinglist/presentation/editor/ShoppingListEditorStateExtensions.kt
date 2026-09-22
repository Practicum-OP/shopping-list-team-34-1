package ru.practicum.shoppinglist.presentation.editor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Dialog
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.State
import ru.practicum.shoppinglist.presentation.lists.ShoppingListIconRegistry

internal fun MutableStateFlow<State>.showItemForm(item: ShoppingItem? = null) {
    showDialog(
        Dialog.ItemForm(
            item = item,
            name = item?.name.orEmpty(),
            quantity = item?.quantity.toEditableQuantity(),
            unit = item?.unit,
        ),
    )
}

internal fun MutableStateFlow<State>.showRenameList(
    name: String,
    iconKey: String?,
) {
    showDialog(
        Dialog.RenameList(
            name = name,
            iconKey = ShoppingListIconRegistry.resolve(iconKey).key,
        ),
    )
}

internal fun MutableStateFlow<State>.changeItemName(value: String) {
    updateItemForm { form -> form.copy(name = value, nameError = false) }
}

internal fun MutableStateFlow<State>.changeItemQuantity(value: String) {
    updateItemForm { form -> form.copy(quantity = value, quantityError = false) }
}

internal fun MutableStateFlow<State>.changeItemUnit(value: MeasurementUnit?) {
    updateItemForm { form -> form.copy(unit = value) }
}

internal fun MutableStateFlow<State>.changeListName(value: String) {
    val dialog = this.value.dialog as? Dialog.RenameList ?: return
    showDialog(dialog.copy(name = value, hasError = false))
}

internal fun MutableStateFlow<State>.changeListIcon(iconKey: String) {
    val dialog = value.dialog as? Dialog.RenameList ?: return
    val resolvedIconKey = ShoppingListIconRegistry.resolve(iconKey).key
    showDialog(dialog.copy(iconKey = resolvedIconKey))
}

private fun MutableStateFlow<State>.updateItemForm(
    transform: (Dialog.ItemForm) -> Dialog.ItemForm,
) {
    val form = value.dialog as? Dialog.ItemForm ?: return
    showDialog(transform(form))
}

internal fun MutableStateFlow<State>.showDialog(dialog: Dialog) {
    update { state -> state.copy(dialog = dialog) }
}

internal fun MutableStateFlow<State>.dismissDialog() {
    update { state -> state.copy(dialog = null) }
}
