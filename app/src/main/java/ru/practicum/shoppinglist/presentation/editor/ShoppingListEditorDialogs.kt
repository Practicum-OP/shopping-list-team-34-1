package ru.practicum.shoppinglist.presentation.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Dialog
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Intent
import ru.practicum.shoppinglist.presentation.lists.ShoppingListIconRegistry

@Composable
internal fun EditorDialog(
    dialog: Dialog?,
    onIntent: (Intent) -> Unit,
) {
    when (dialog) {
        is Dialog.ItemForm -> ItemFormDialog(dialog, onIntent)
        is Dialog.RenameList -> RenameListDialog(dialog, onIntent)
        is Dialog.DeleteItem -> ConfirmationDialog(
            title = R.string.editor_delete_item_title,
            message = stringResource(
                R.string.editor_delete_item_message,
                dialog.item.name,
            ),
            confirmLabel = R.string.editor_delete,
            confirmIntent = Intent.ConfirmDeleteItemClicked,
            onIntent = onIntent,
        )
        Dialog.DeleteList -> ConfirmationDialog(
            title = R.string.editor_delete_list_title,
            message = stringResource(R.string.editor_delete_list_message),
            confirmLabel = R.string.editor_delete,
            confirmIntent = Intent.ConfirmDeleteListClicked,
            onIntent = onIntent,
        )
        Dialog.ClearPurchased -> ConfirmationDialog(
            title = R.string.editor_clear_purchased_title,
            message = stringResource(R.string.editor_clear_purchased_message),
            confirmLabel = R.string.editor_clear,
            confirmIntent = Intent.ConfirmClearPurchasedClicked,
            onIntent = onIntent,
        )
        null -> Unit
    }
}

@Composable
private fun ItemFormDialog(
    form: Dialog.ItemForm,
    onIntent: (Intent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!form.isSaving) onIntent(Intent.DialogDismissed)
        },
        title = {
            Text(
                text = stringResource(
                    if (form.item == null) {
                        R.string.editor_add_item_title
                    } else {
                        R.string.editor_edit_item_title
                    },
                ),
            )
        },
        text = { ItemFormFields(form = form, onIntent = onIntent) },
        confirmButton = {
            TextButton(
                enabled = !form.isSaving && form.name.isNotBlank(),
                onClick = { onIntent(Intent.SaveItemClicked) },
            ) {
                Text(stringResource(R.string.editor_save))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !form.isSaving,
                onClick = { onIntent(Intent.DialogDismissed) },
            ) {
                Text(stringResource(R.string.editor_cancel))
            }
        },
    )
}

@Composable
private fun ItemFormFields(
    form: Dialog.ItemForm,
    onIntent: (Intent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ItemNameField(form = form, onIntent = onIntent)
        ItemQuantityField(form = form, onIntent = onIntent)
        MeasurementUnitField(
            selectedUnit = form.unit,
            enabled = !form.isSaving,
            onUnitSelected = { unit ->
                onIntent(Intent.ItemUnitChanged(unit))
            },
        )
    }
}

@Composable
private fun ItemNameField(
    form: Dialog.ItemForm,
    onIntent: (Intent) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = form.name,
        enabled = !form.isSaving,
        onValueChange = { value -> onIntent(Intent.ItemNameChanged(value)) },
        label = { Text(stringResource(R.string.editor_item_name)) },
        singleLine = true,
        isError = form.nameError,
        supportingText = if (form.nameError) {
            { Text(stringResource(R.string.editor_item_name_required)) }
        } else {
            null
        },
    )
}

@Composable
private fun ItemQuantityField(
    form: Dialog.ItemForm,
    onIntent: (Intent) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = form.quantity,
        enabled = !form.isSaving,
        onValueChange = { value -> onIntent(Intent.ItemQuantityChanged(value)) },
        label = { Text(stringResource(R.string.editor_item_quantity)) },
        singleLine = true,
        isError = form.quantityError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = if (form.quantityError) {
            { Text(stringResource(R.string.editor_item_quantity_invalid)) }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementUnitField(
    selectedUnit: MeasurementUnit?,
    enabled: Boolean,
    onUnitSelected: (MeasurementUnit?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            value = selectedUnit?.displayName()
                ?: stringResource(R.string.editor_unit_none),
            enabled = enabled,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.editor_item_unit)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_unit_none)) },
                onClick = {
                    expanded = false
                    onUnitSelected(null)
                },
            )
            MeasurementUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.displayName()) },
                    onClick = {
                        expanded = false
                        onUnitSelected(unit)
                    },
                )
            }
        }
    }
}

@Composable
private fun RenameListDialog(
    dialog: Dialog.RenameList,
    onIntent: (Intent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!dialog.isSaving) onIntent(Intent.DialogDismissed)
        },
        title = { Text(stringResource(R.string.editor_rename_list_title)) },
        text = { RenameListFields(dialog = dialog, onIntent = onIntent) },
        confirmButton = {
            TextButton(
                enabled = !dialog.isSaving && dialog.name.isNotBlank(),
                onClick = { onIntent(Intent.SaveListNameClicked) },
            ) {
                Text(stringResource(R.string.editor_save))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !dialog.isSaving,
                onClick = { onIntent(Intent.DialogDismissed) },
            ) {
                Text(stringResource(R.string.editor_cancel))
            }
        },
    )
}

@Composable
private fun RenameListFields(
    dialog: Dialog.RenameList,
    onIntent: (Intent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = dialog.name,
            enabled = !dialog.isSaving,
            onValueChange = { value -> onIntent(Intent.ListNameChanged(value)) },
            label = { Text(stringResource(R.string.editor_list_name)) },
            singleLine = true,
            isError = dialog.hasError,
            supportingText = if (dialog.hasError) {
                { Text(stringResource(R.string.editor_list_name_required)) }
            } else {
                null
            },
        )
        IconPicker(
            selectedKey = dialog.iconKey,
            enabled = !dialog.isSaving,
            onSelected = { iconKey -> onIntent(Intent.ListIconChanged(iconKey)) },
        )
    }
}

@Composable
private fun IconPicker(
    selectedKey: String,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShoppingListIconRegistry.options.forEach { option ->
            FilterChip(
                selected = option.key == selectedKey,
                enabled = enabled,
                onClick = { onSelected(option.key) },
                label = { Text(stringResource(option.labelRes)) },
                leadingIcon = {
                    Icon(
                        imageVector = option.imageVector,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    @StringRes title: Int,
    message: String,
    @StringRes confirmLabel: Int,
    confirmIntent: Intent,
    onIntent: (Intent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onIntent(Intent.DialogDismissed) },
        title = { Text(stringResource(title)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onIntent(confirmIntent) }) {
                Text(stringResource(confirmLabel))
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(Intent.DialogDismissed) }) {
                Text(stringResource(R.string.editor_cancel))
            }
        },
    )
}
