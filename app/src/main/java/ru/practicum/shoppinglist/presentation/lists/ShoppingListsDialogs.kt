package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

private const val CREATE_DIALOG_TEST_TAG = "create_list_dialog"
private const val CREATE_NAME_TEST_TAG = "create_list_name"
private const val DELETE_DIALOG_TEST_TAG = "delete_list_dialog"

@Composable
internal fun CreateListDialog(
    state: CreateListDialogState,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag(CREATE_DIALOG_TEST_TAG),
        onDismissRequest = { onAction(ShoppingListsAction.CreateDialogDismissed) },
        title = { Text(stringResource(R.string.lists_create_dialog_title)) },
        text = {
            CreateListForm(
                state = state,
                isSubmitting = isSubmitting,
                onAction = onAction,
            )
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_create),
                isSubmitting = isSubmitting,
                isActionEnabled = state.name.isNotBlank(),
                onClick = { onAction(ShoppingListsAction.CreateConfirmed) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onAction(ShoppingListsAction.CreateDialogDismissed) },
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun CreateListForm(
    state: CreateListDialogState,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CREATE_NAME_TEST_TAG),
            value = state.name,
            enabled = !isSubmitting,
            onValueChange = { onAction(ShoppingListsAction.CreateNameChanged(it)) },
            label = { Text(stringResource(R.string.lists_name_label)) },
            isError = state.showNameError,
            supportingText = if (state.showNameError) {
                { Text(stringResource(R.string.lists_name_required)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onAction(ShoppingListsAction.CreateConfirmed) },
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.lists_choose_icon),
                style = MaterialTheme.typography.labelLarge,
            )
            IconPicker(
                selectedIconKey = state.iconKey,
                enabled = !isSubmitting,
                onIconSelected = {
                    onAction(ShoppingListsAction.CreateIconSelected(it))
                },
            )
        }
    }
}

@Composable
private fun IconPicker(
    selectedIconKey: String,
    enabled: Boolean,
    onIconSelected: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(
            items = ShoppingListIconRegistry.options,
            key = ShoppingListIconOption::key,
        ) { option ->
            val isSelected = option.key == selectedIconKey
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onIconSelected(option.key) },
                    ),
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                ),
            ) {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    imageVector = option.imageVector,
                    contentDescription = stringResource(option.labelRes),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

@Composable
internal fun DeleteListDialog(
    shoppingList: ShoppingList,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag(DELETE_DIALOG_TEST_TAG),
        onDismissRequest = { onAction(ShoppingListsAction.DeleteDialogDismissed) },
        title = { Text(stringResource(R.string.lists_delete_dialog_title)) },
        text = {
            Text(stringResource(R.string.lists_delete_dialog_body, shoppingList.name))
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = { onAction(ShoppingListsAction.DeleteConfirmed) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onAction(ShoppingListsAction.DeleteDialogDismissed) },
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(label)
        }
    }
}
