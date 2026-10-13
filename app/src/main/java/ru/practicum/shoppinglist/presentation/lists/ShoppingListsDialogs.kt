package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MobileFriendly
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

private const val CREATE_DIALOG_TEST_TAG = "create_list_dialog"
private const val CREATE_NAME_TEST_TAG = "create_list_name"
private const val RENAME_DIALOG_TEST_TAG = "rename_list_dialog"
private const val RENAME_NAME_TEST_TAG = "rename_list_name"
private const val DELETE_DIALOG_TEST_TAG = "delete_list_dialog"

@Composable
internal fun CreateListDialog(
    state: CreateListDialogState,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(312.dp).testTag(CREATE_DIALOG_TEST_TAG),
        onDismissRequest = { onAction(ShoppingListsAction.CreateDialogDismissed) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.figma_lists_docs_add_on),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        },
        title = { Text(stringResource(R.string.lists_create_dialog_title)) },
        text = {
            ListForm(
                name = state.name,
                iconKey = state.iconKey,
                showNameError = state.showNameError,
                isSubmitting = isSubmitting,
                nameTestTag = CREATE_NAME_TEST_TAG,
                onNameChanged = {
                    onAction(ShoppingListsAction.CreateNameChanged(it))
                },
                onIconSelected = {
                    onAction(ShoppingListsAction.CreateIconSelected(it))
                },
                onConfirmed = { onAction(ShoppingListsAction.CreateConfirmed) },
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
private fun ListForm(
    name: String,
    iconKey: String,
    showNameError: Boolean,
    isSubmitting: Boolean,
    nameTestTag: String,
    onNameChanged: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onConfirmed: () -> Unit,
) {
    var showIconPicker by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ListFormIcon(iconKey, isSubmitting) { showIconPicker = true }
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag(nameTestTag),
            value = name,
            enabled = !isSubmitting,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.lists_name_label)) },
            isError = showNameError,
            supportingText = if (showNameError) {
                { Text(stringResource(R.string.lists_name_required)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onConfirmed() },
            ),
        )
    }
    if (showIconPicker) {
        ShoppingListIconPickerSheet(
            selectedIconKey = iconKey,
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIconKey ->
                showIconPicker = false
                onIconSelected(selectedIconKey)
            },
        )
    }
}

@Composable
private fun ListFormIcon(iconKey: String, isSubmitting: Boolean, onClick: () -> Unit) {
    val selectedOption = ShoppingListIconRegistry.resolve(iconKey)
    IconButton(enabled = !isSubmitting, onClick = onClick) {
        Icon(
            imageVector = selectedOption.imageVector,
            contentDescription = stringResource(R.string.lists_choose_icon),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
internal fun RenameListDialog(
    state: RenameListDialogState,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(312.dp).testTag(RENAME_DIALOG_TEST_TAG),
        onDismissRequest = { onAction(ShoppingListsAction.RenameDialogDismissed) },
        title = { Text(stringResource(R.string.lists_rename_dialog_title)) },
        text = {
            ListForm(
                name = state.name,
                iconKey = state.iconKey,
                showNameError = state.showNameError,
                isSubmitting = isSubmitting,
                nameTestTag = RENAME_NAME_TEST_TAG,
                onNameChanged = {
                    onAction(ShoppingListsAction.RenameNameChanged(it))
                },
                onIconSelected = {
                    onAction(ShoppingListsAction.RenameIconSelected(it))
                },
                onConfirmed = { onAction(ShoppingListsAction.RenameConfirmed) },
            )
        },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_rename),
                isSubmitting = isSubmitting,
                isActionEnabled = state.name.isNotBlank(),
                onClick = { onAction(ShoppingListsAction.RenameConfirmed) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onAction(ShoppingListsAction.RenameDialogDismissed) },
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun DeleteListDialog(
    shoppingList: ShoppingList,
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(312.dp).testTag(DELETE_DIALOG_TEST_TAG),
        onDismissRequest = { onAction(ShoppingListsAction.DeleteDialogDismissed) },
        icon = { Icon(Icons.Outlined.MobileFriendly, contentDescription = null) },
        title = {
            Text(stringResource(R.string.lists_delete_dialog_title, shoppingList.name))
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
internal fun DeleteAllListsDialog(
    isSubmitting: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.width(312.dp).testTag("delete_all_lists_dialog"),
        onDismissRequest = { onAction(ShoppingListsAction.DeleteAllListsDismissed) },
        icon = { Icon(Icons.Outlined.MobileFriendly, contentDescription = null) },
        title = { Text(stringResource(R.string.lists_delete_all_dialog_title)) },
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.action_delete),
                isSubmitting = isSubmitting,
                isActionEnabled = true,
                onClick = { onAction(ShoppingListsAction.DeleteAllListsConfirmed) },
            )
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onAction(ShoppingListsAction.DeleteAllListsDismissed) },
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
    TextButton(
        enabled = !isSubmitting && isActionEnabled,
        onClick = onClick,
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
