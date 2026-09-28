package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

private const val CREATE_SHEET_TEST_TAG = "create_item_sheet"
private const val CREATE_NAME_TEST_TAG = "create_item_name"
private const val DELETE_DIALOG_TEST_TAG = "delete_item_dialog"
private const val CLEAR_DIALOG_TEST_TAG = "clear_list_dialog"

val DialogBackgroundColor = Color(0xFFF4E6DA)
val DialogPrimaryButtonColor = Color(0xFF845416)
val DialogOnPrimaryButtonColor = Color(0xFFFFFFFF)
val DialogSecondaryButtonColor = Color(0xFFFEDDBD)
val DialogOnSecondaryButtonColor = Color(0xFF281805)
val BottomSheetBackgroundColor = Color(0xFFFFF1E7)

private val OnSurfaceColor = Color(0xFF211A14)
private val OnSurfaceVariantColor = Color(0xFF50453A)
private val PrimaryContainerColor = Color(0xFFFFDCBB)
private val OnPrimaryContainerColor = Color(0xFF2B1700)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateItemBottomSheet(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = Modifier.testTag(CREATE_SHEET_TEST_TAG),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BottomSheetBackgroundColor,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.editor_create_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceColor,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ItemForm(
                name = state.name,
                quantity = state.quantity,
                selectedUnit = state.selectedUnit,
                showNameError = state.showNameError,
                showQuantityError = state.showQuantityError,
                isSubmitting = isSubmitting,
                nameTestTag = CREATE_NAME_TEST_TAG,
                onNameChanged = { onIntent(ListEditorIntent.NameChanged(it)) },
                onQuantityChanged = { onIntent(ListEditorIntent.QuantityChanged(it)) },
                onUnitChanged = { onIntent(ListEditorIntent.UnitChanged(it)) },
                onDone = { onIntent(ListEditorIntent.ConfirmAddItem) },
            )
            Button(
                onClick = { onIntent(ListEditorIntent.ConfirmAddItem) },
                enabled = !isSubmitting && state.name.isNotBlank() && state.quantity > 0,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainerColor,
                    contentColor = OnPrimaryContainerColor,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = OnPrimaryContainerColor,
                    )
                } else {
                    Text(text = stringResource(R.string.action_add), style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemForm(
    name: String,
    quantity: Int,
    selectedUnit: MeasurementUnit,
    showNameError: Boolean,
    showQuantityError: Boolean,
    isSubmitting: Boolean,
    nameTestTag: String,
    onNameChanged: (String) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onUnitChanged: (MeasurementUnit) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag(nameTestTag),
            value = name,
            enabled = !isSubmitting,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.editor_name_label)) },
            isError = showNameError,
            supportingText = if (showNameError) { { Text(stringResource(R.string.editor_name_required)) } } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = quantity.toString(),
            enabled = !isSubmitting,
            onValueChange = {
                val newQuantity = it.toIntOrNull() ?: 0
                onQuantityChanged(newQuantity)
            },
            label = { Text(stringResource(R.string.editor_quantity_label)) },
            isError = showQuantityError,
            supportingText = if (showQuantityError) { { Text(stringResource(R.string.editor_quantity_required)) } } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = stringResource(selectedUnit.getShortNameRes()),
                onValueChange = {},
                readOnly = true,
                enabled = !isSubmitting,
                label = { Text(stringResource(R.string.editor_unit_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                enumValues<MeasurementUnit>().forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(stringResource(unit.getShortNameRes())) },
                        onClick = { onUnitChanged(unit); expanded = false }
                    )
                }
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
        containerColor = DialogBackgroundColor,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(text = stringResource(R.string.editor_delete_dialog_title), color = OnSurfaceColor) },
        text = { Text(text = stringResource(R.string.editor_delete_dialog_body, shoppingItem.name), color = OnSurfaceVariantColor) },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                isPrimary = true,
                onClick = { onIntent(ListEditorIntent.ItemDeleted(shoppingItem.id)) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onIntent(ListEditorIntent.DeleteCancelled) },
                colors = ButtonDefaults.textButtonColors(contentColor = DialogOnSecondaryButtonColor)
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
        onDismissRequest = { onIntent(ListEditorIntent.ClearCancelled) },
        containerColor = DialogBackgroundColor,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(text = stringResource(R.string.editor_clear_dialog_title), color = OnSurfaceColor) },
        text = { Text(text = stringResource(R.string.editor_clear_dialog_body), color = OnSurfaceVariantColor) },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_yes),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                isPrimary = true,
                onClick = { onIntent(ListEditorIntent.ClearConfirmed) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onIntent(ListEditorIntent.ClearCancelled) },
                colors = ButtonDefaults.textButtonColors(contentColor = DialogOnSecondaryButtonColor)
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
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    Button(
        enabled = !isSubmitting && isActionEnabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) DialogPrimaryButtonColor else DialogSecondaryButtonColor,
            contentColor = if (isPrimary) DialogOnPrimaryButtonColor else DialogOnSecondaryButtonColor,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = if (isPrimary) DialogOnPrimaryButtonColor else DialogOnSecondaryButtonColor,
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