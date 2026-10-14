package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import kotlin.enums.enumEntries

private const val CREATE_NAME_TEST_TAG = "create_item_name"
private const val SUGGESTIONS_TEST_TAG = "product_name_suggestions"
private const val DELETE_DIALOG_TEST_TAG = "delete_item_dialog"
private const val CLEAR_DIALOG_TEST_TAG = "clear_list_dialog"

@Composable
internal fun CreateItemSheetContent(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
    ) {
        ItemForm(state = state, isSubmitting = isSubmitting, onIntent = onIntent)
    }
}

@Composable
private fun ItemForm(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ItemNameField(state = state, isSubmitting = isSubmitting, onIntent = onIntent)
        ItemQuantityControls(state = state, isSubmitting = isSubmitting, onIntent = onIntent)
    }
}

@Composable
private fun ItemQuantityControls(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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
        QuantityActionButtons(state = state, isSubmitting = isSubmitting, onIntent = onIntent)
    }
}

@Composable
private fun QuantityActionButtons(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val canDecrease = !isSubmitting && state.quantity > 1
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuantityButton(
            icon = Icons.Filled.Remove,
            contentDescription = stringResource(R.string.editor_decrease_quantity),
            enabled = canDecrease,
            onClick = { onIntent(ListEditorIntent.QuantityChanged(state.quantity - 1)) },
        )
        QuantityButton(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.editor_increase_quantity),
            enabled = !isSubmitting,
            onClick = { onIntent(ListEditorIntent.QuantityChanged(state.quantity + 1)) },
        )
    }
}

@Composable
private fun QuantityButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
private fun ItemNameField(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val suggestionsVisible = state.suggestions.isNotEmpty() && !isSubmitting
    var textFieldHeightPx by remember { mutableIntStateOf(0) }
    var menuHeightPx by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxWidth()) {
        NameTextField(
            state = state,
            isSubmitting = isSubmitting,
            onIntent = onIntent,
            onHeightChanged = { textFieldHeightPx = it },
        )
        SuggestionsDropdown(
            suggestions = state.suggestions,
            expanded = suggestionsVisible,
            menuHeightPx = menuHeightPx,
            textFieldHeightPx = textFieldHeightPx,
            onHeightChanged = { menuHeightPx = it },
            onIntent = onIntent,
        )
    }
}

@Composable
private fun NameTextField(
    state: CreateItemDialogState,
    isSubmitting: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
    onHeightChanged: (Int) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onHeightChanged(it.size.height) }
            .testTag(CREATE_NAME_TEST_TAG),
        value = state.name,
        enabled = !isSubmitting,
        onValueChange = { onIntent(ListEditorIntent.NameChanged(it)) },
        label = { Text(stringResource(R.string.editor_name_label)) },
        placeholder = { Text(stringResource(R.string.editor_product_placeholder)) },
        isError = state.showNameError,
        supportingText = if (state.showNameError) {
            { Text(stringResource(R.string.editor_name_required)) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { onIntent(ListEditorIntent.ConfirmAddItem) },
        ),
    )
}

@Composable
private fun SuggestionsDropdown(
    suggestions: List<String>,
    expanded: Boolean,
    menuHeightPx: Int,
    textFieldHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val offsetY = with(density) {
        if (imeHeight > 0) (-menuHeightPx - textFieldHeightPx).toDp() else 0.dp
    }
    DropdownMenu(
        modifier = Modifier
            .heightIn(max = 200.dp)
            .onGloballyPositioned { onHeightChanged(it.size.height) }
            .testTag(SUGGESTIONS_TEST_TAG),
        offset = DpOffset(x = 0.dp, y = offsetY),
        expanded = expanded,
        onDismissRequest = { onIntent(ListEditorIntent.SuggestionsDismissed) },
        properties = PopupProperties(focusable = false),
    ) {
        suggestions.forEach { suggestion ->
            DropdownMenuItem(
                text = { Text(text = suggestion, maxLines = 1) },
                onClick = { onIntent(ListEditorIntent.SuggestionSelected(suggestion)) },
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
        onValueChange = { onQuantityChanged(it.toIntOrNull() ?: 0) },
        label = { Text(stringResource(R.string.editor_quantity_label)) },
        isError = showError,
        supportingText = if (showError) {
            { Text(stringResource(R.string.editor_quantity_required)) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = stringResource(selectedUnit.getShortNameRes()),
            onValueChange = {},
            readOnly = true,
            enabled = !isSubmitting,
            label = { Text(stringResource(R.string.editor_unit_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            enumEntries<MeasurementUnit>().forEach { unit ->
                DropdownMenuItem(
                    text = { Text(stringResource(unit.getShortNameRes())) },
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
        icon = { DialogErrorIcon() },
        title = {
            Text(
                text = stringResource(R.string.editor_delete_dialog_title),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.editor_delete_dialog_body, shoppingItem.name),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            ClearDialogDismissButton(isSubmitting, onIntent)
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
        onDismissRequest = { onIntent(ListEditorIntent.ClearCancelled) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        icon = { DialogErrorIcon() },
        title = {
            Text(text = stringResource(titleRes), color = MaterialTheme.colorScheme.onSurface)
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = { onIntent(ListEditorIntent.ClearConfirmed) },
            )
        },
        dismissButton = { ClearDialogDismissButton(isSubmitting, onIntent) },
    )
}

@Composable
private fun DialogErrorIcon() {
    Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
internal fun MeasurementUnit.getShortNameRes(): Int = when (this) {
    MeasurementUnit.PIECE -> R.string.unit_piece_short
    MeasurementUnit.KILOGRAM -> R.string.unit_kilogram_short
    MeasurementUnit.LITER -> R.string.unit_liter_short
    MeasurementUnit.MILLILITER -> R.string.unit_milliliter_short
    MeasurementUnit.GRAM -> R.string.unit_gram_short
}
