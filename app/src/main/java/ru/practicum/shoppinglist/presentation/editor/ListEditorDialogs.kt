package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import kotlin.enums.enumEntries

private const val CREATE_SHEET_TEST_TAG = "create_item_sheet"
private const val CREATE_NAME_TEST_TAG = "create_item_name"
private const val DELETE_DIALOG_TEST_TAG = "delete_item_dialog"
private const val CLEAR_DIALOG_TEST_TAG = "clear_list_dialog"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateItemBottomSheet(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
    isEditing: Boolean,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        modifier = Modifier.testTag(CREATE_SHEET_TEST_TAG),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large,
    ) {
        CreateItemSheetContent(
            state = state,
            isSubmitting = isSubmitting,
            isEditing = isEditing,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun CreateItemSheetContent(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    isEditing: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(
                if (isEditing) {
                    R.string.editor_edit_dialog_title
                } else {
                    R.string.editor_create_dialog_title
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ItemForm(
            state = state,
            isSubmitting = isSubmitting,
            onIntent = onIntent,
        )

        SubmitItemButton(
            state = state,
            isSubmitting = isSubmitting,
            isEditing = isEditing,
            onClick = {
                onIntent(ListEditorIntent.ConfirmAddItem)
            },
        )

        Spacer(
            modifier = Modifier.height(
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding(),
            ),
        )
    }
}

@Composable
private fun SubmitItemButton(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    isEditing: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting &&
                state.name.isNotBlank() &&
                state.quantity > 0,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = stringResource(
                    if (isEditing) {
                        R.string.action_edit
                    } else {
                        R.string.action_add
                    },
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ItemForm(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ItemNameField(
            name = state.name,
            showError = state.showNameError,
            isSubmitting = isSubmitting,
            onNameChanged = {
                onIntent(ListEditorIntent.NameChanged(it))
            },
            onDone = {
                onIntent(ListEditorIntent.ConfirmAddItem)
            },
        )

        ItemQuantityField(
            quantity = state.quantity,
            showError = state.showQuantityError,
            isSubmitting = isSubmitting,
            onQuantityChanged = {
                onIntent(ListEditorIntent.QuantityChanged(it))
            },
        )

        MeasurementUnitField(
            selectedUnit = state.selectedUnit,
            isSubmitting = isSubmitting,
            onUnitChanged = {
                onIntent(ListEditorIntent.UnitChanged(it))
            },
        )
    }
}

@Composable
private fun ItemNameField(
    name: String,
    showError: Boolean,
    isSubmitting: Boolean,
    onNameChanged: (String) -> Unit,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CREATE_NAME_TEST_TAG),
        value = name,
        enabled = !isSubmitting,
        onValueChange = onNameChanged,
        label = {
            Text(stringResource(R.string.editor_name_label))
        },
        isError = showError,
        supportingText = if (showError) {
            {
                Text(stringResource(R.string.editor_name_required))
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() },
        ),
    )
}

@Composable
private fun ItemQuantityField(
    quantity: Int,
    showError: Boolean,
    isSubmitting: Boolean,
    onQuantityChanged: (Int) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = quantity.toString(),
        enabled = !isSubmitting,
        onValueChange = { value ->
            onQuantityChanged(value.toIntOrNull() ?: 0)
        },
        label = {
            Text(stringResource(R.string.editor_quantity_label))
        },
        isError = showError,
        supportingText = if (showError) {
            {
                Text(stringResource(R.string.editor_quantity_required))
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementUnitField(
    selectedUnit: MeasurementUnit,
    isSubmitting: Boolean,
    onUnitChanged: (MeasurementUnit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
    ) {
        OutlinedTextField(
            value = stringResource(selectedUnit.getShortNameRes()),
            onValueChange = {},
            readOnly = true,
            enabled = !isSubmitting,
            label = {
                Text(stringResource(R.string.editor_unit_label))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            enumEntries<MeasurementUnit>().forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(unit.getShortNameRes()))
                    },
                    onClick = {
                        onUnitChanged(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
internal fun DeleteItemDialog(
    shoppingItem: ShoppingItem,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag(DELETE_DIALOG_TEST_TAG),
        onDismissRequest = { onIntent(ListEditorIntent.DeleteCancelled) },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = stringResource(R.string.editor_delete_dialog_title),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(R.string.editor_delete_dialog_body, shoppingItem.name),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = { onIntent(ListEditorIntent.ItemDeleted(shoppingItem.id)) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onIntent(ListEditorIntent.DeleteCancelled) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun ClearListDialog(
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag(CLEAR_DIALOG_TEST_TAG),
        onDismissRequest = {
            onIntent(ListEditorIntent.ClearCancelled)
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = stringResource(R.string.editor_clear_dialog_title),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.editor_clear_dialog_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_yes),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = {
                    onIntent(ListEditorIntent.ClearConfirmed)
                },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    onIntent(ListEditorIntent.ClearCancelled)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun DialogConfirmButton(
    label: String,
    isSubmitting: Boolean,
    isActionEnabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        enabled = !isSubmitting && isActionEnabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onError,
            )
        } else {
            Text(label)
        }
    }
}

@Composable
internal fun MeasurementUnit.getShortNameRes(): Int {
    return when (this) {
        MeasurementUnit.PIECE -> R.string.unit_piece_short
        MeasurementUnit.KILOGRAM -> R.string.unit_kilogram_short
        MeasurementUnit.LITER -> R.string.unit_liter_short
        MeasurementUnit.MILLILITER -> R.string.unit_milliliter_short
        MeasurementUnit.GRAM -> R.string.unit_gram_short
    }
}
