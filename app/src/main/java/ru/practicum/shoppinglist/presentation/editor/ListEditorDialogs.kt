package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
private const val SUGGESTIONS_TEST_TAG = "product_name_suggestions"
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
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .testTag(CREATE_SHEET_TEST_TAG),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { EditorSheetDragHandle() },
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
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
    ) {
        ItemForm(
            state = state,
            isSubmitting = isSubmitting,
            isEditing = isEditing,
            onIntent = onIntent,
        )
    }
}

@Composable
internal fun EditorSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(width = 32.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(2.dp),
                ),
        )
    }
}

@Composable
private fun ItemForm(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    isEditing: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ItemNameField(
            state = state,
            isSubmitting = isSubmitting,
            onIntent = onIntent,
        )
        ItemQuantityControls(state, isSubmitting, isEditing, onIntent)
    }
}

@Composable
private fun ItemQuantityControls(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    isEditing: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ItemQuantityField(
            modifier = Modifier.weight(1f),
            quantity = state.quantity,
            showError = state.showQuantityError,
            isSubmitting = isSubmitting,
            onQuantityChanged = { onIntent(ListEditorIntent.QuantityChanged(it)) },
        )
        MeasurementUnitField(
            modifier = Modifier.weight(1f),
            selectedUnit = state.selectedUnit,
            isSubmitting = isSubmitting,
            onUnitChanged = { onIntent(ListEditorIntent.UnitChanged(it)) },
        )
        QuantityActionButtons(state, isSubmitting, isEditing, onIntent)
    }
}

@Composable
private fun QuantityActionButtons(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    isEditing: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Row {
        IconButton(
            onClick = {
                onIntent(ListEditorIntent.QuantityChanged((state.quantity - 1).coerceAtLeast(1)))
            },
            enabled = !isSubmitting && state.quantity > 1,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = stringResource(R.string.editor_decrease_quantity),
            )
        }
        IconButton(
            onClick = { onIntent(ListEditorIntent.ConfirmAddItem) },
            enabled = !isSubmitting && state.name.isNotBlank() && state.quantity > 0,
            modifier = Modifier.size(48.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(
                        if (isEditing) R.string.action_edit else R.string.action_add,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemNameField(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val suggestionsVisible = state.suggestions.isNotEmpty() && !isSubmitting
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = suggestionsVisible,
        onExpandedChange = { expanded ->
            if (!expanded) {
                onIntent(ListEditorIntent.SuggestionsDismissed)
            }
        },
    ) {
        ProductNameTextField(
            state = state,
            isSubmitting = isSubmitting,
            onIntent = onIntent,
        )
        ProductSuggestionsMenu(state.suggestions, suggestionsVisible, onIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.ProductNameTextField(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .menuAnchor(
                type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                enabled = !isSubmitting,
            )
            .testTag(CREATE_NAME_TEST_TAG),
        value = state.name,
        enabled = !isSubmitting,
        onValueChange = { name ->
            onIntent(ListEditorIntent.NameChanged(name))
        },
        label = { Text(stringResource(R.string.editor_name_label)) },
        placeholder = { Text(stringResource(R.string.editor_product_placeholder)) },
        isError = state.showNameError,
        supportingText = if (state.showNameError) {
            { Text(stringResource(R.string.editor_name_required)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { onIntent(ListEditorIntent.ConfirmAddItem) },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.ProductSuggestionsMenu(
    suggestions: List<String>,
    expanded: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    ExposedDropdownMenu(
        modifier = Modifier.testTag(SUGGESTIONS_TEST_TAG),
        expanded = expanded,
        onDismissRequest = {
            onIntent(ListEditorIntent.SuggestionsDismissed)
        },
    ) {
        suggestions.forEach { suggestion ->
            DropdownMenuItem(
                text = { Text(text = suggestion, maxLines = 1) },
                onClick = {
                    onIntent(ListEditorIntent.SuggestionSelected(suggestion))
                },
            )
        }
    }
}

@Composable
private fun ItemQuantityField(
    modifier: Modifier,
    quantity: Int,
    showError: Boolean,
    isSubmitting: Boolean,
    onQuantityChanged: (Int) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.height(64.dp),
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
    modifier: Modifier,
    selectedUnit: MeasurementUnit,
    isSubmitting: Boolean,
    onUnitChanged: (MeasurementUnit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
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
                .height(64.dp)
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
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
            Button(
                enabled = !isSubmitting,
                onClick = { onIntent(ListEditorIntent.DeleteCancelled) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
    purchasedOnly: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val titleRes = if (purchasedOnly) {
        R.string.editor_clear_purchased_dialog_title
    } else {
        R.string.editor_clear_dialog_title
    }
    AlertDialog(
        modifier = Modifier.testTag(CLEAR_DIALOG_TEST_TAG),
        onDismissRequest = {
            onIntent(ListEditorIntent.ClearCancelled)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        title = {
            Text(
                text = stringResource(titleRes),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = {
                    onIntent(ListEditorIntent.ClearConfirmed)
                },
            )
        },
        dismissButton = {
            ClearDialogDismissButton(isSubmitting, onIntent)
        },
    )
}

@Composable
private fun ClearDialogDismissButton(
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Button(
        enabled = !isSubmitting,
        onClick = { onIntent(ListEditorIntent.ClearCancelled) },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Text(stringResource(R.string.action_cancel))
    }
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
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
