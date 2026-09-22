package ru.practicum.shoppinglist.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Intent
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.State

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShoppingListEditorContent(
    state: State,
    snackbarHost: @Composable () -> Unit,
    onBack: () -> Unit,
    onIntent: (Intent) -> Unit,
) {
    val canEdit = !state.isLoading &&
        !state.isDeletingList &&
        state.loadError == null &&
        state.listName.isNotBlank()

    BackHandler(enabled = state.isDeletingList) {
        // Keep the screen alive until the confirmed Room deletion finishes.
    }

    Scaffold(
        topBar = {
            EditorTopBar(
                listName = state.listName,
                sort = state.sort,
                hasPurchasedItems = state.items.any(ShoppingItem::isPurchased),
                backEnabled = !state.isDeletingList,
                actionsEnabled = canEdit,
                onBack = onBack,
                onIntent = onIntent,
            )
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            if (canEdit) {
                ExtendedFloatingActionButton(
                    onClick = { onIntent(Intent.AddItemClicked) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(R.string.editor_add_item)) },
                )
            }
        },
    ) { contentPadding ->
        EditorBody(
            state = state,
            contentPadding = contentPadding,
            onIntent = onIntent,
        )
    }

    EditorDialog(
        dialog = state.dialog,
        onIntent = onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    listName: String,
    sort: ShoppingItemSort,
    hasPurchasedItems: Boolean,
    backEnabled: Boolean,
    actionsEnabled: Boolean,
    onBack: () -> Unit,
    onIntent: (Intent) -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = listName,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(
                enabled = backEnabled,
                onClick = onBack,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.editor_back),
                )
            }
        },
        actions = {
            if (actionsEnabled) {
                SortMenu(sort = sort, onIntent = onIntent)
                ListActionsMenu(
                    hasPurchasedItems = hasPurchasedItems,
                    onIntent = onIntent,
                )
            }
        },
    )
}

@Composable
private fun SortMenu(
    sort: ShoppingItemSort,
    onIntent: (Intent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = stringResource(R.string.editor_sort),
                tint = if (sort == ShoppingItemSort.ALPHABETICAL) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SortMenuItem(
                text = stringResource(R.string.editor_sort_manual),
                selected = sort == ShoppingItemSort.MANUAL,
                onClick = {
                    expanded = false
                    onIntent(Intent.SortChanged(ShoppingItemSort.MANUAL))
                },
            )
            SortMenuItem(
                text = stringResource(R.string.editor_sort_alphabetical),
                selected = sort == ShoppingItemSort.ALPHABETICAL,
                onClick = {
                    expanded = false
                    onIntent(Intent.SortChanged(ShoppingItemSort.ALPHABETICAL))
                },
            )
        }
    }
}

@Composable
private fun SortMenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        modifier = Modifier.semantics { this.selected = selected },
        text = { Text(text) },
        onClick = onClick,
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
    )
}

@Composable
private fun ListActionsMenu(
    hasPurchasedItems: Boolean,
    onIntent: (Intent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.editor_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_rename_list)) },
                onClick = {
                    expanded = false
                    onIntent(Intent.RenameListClicked)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_clear_purchased)) },
                enabled = hasPurchasedItems,
                onClick = {
                    expanded = false
                    onIntent(Intent.ClearPurchasedClicked)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.editor_delete_list),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    expanded = false
                    onIntent(Intent.DeleteListClicked)
                },
            )
        }
    }
}

@Composable
private fun EditorBody(
    state: State,
    contentPadding: PaddingValues,
    onIntent: (Intent) -> Unit,
) {
    when {
        state.isLoading || state.isDeletingList -> LoadingContent(contentPadding)
        state.loadError != null -> ShoppingListEditorErrorContent(
            error = state.loadError,
            contentPadding = contentPadding,
            onRetry = { onIntent(Intent.RetryClicked) },
        )
        state.items.isEmpty() -> EmptyItemsContent(contentPadding)
        else -> ItemsContent(
            items = state.items,
            contentPadding = contentPadding,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyItemsContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.editor_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.editor_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ItemsContent(
    items: List<ShoppingItem>,
    contentPadding: PaddingValues,
    onIntent: (Intent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        items(
            items = items,
            key = ShoppingItem::id,
        ) { item ->
            ShoppingItemRow(item = item, onIntent = onIntent)
            HorizontalDivider()
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onIntent: (Intent) -> Unit,
) {
    val checkboxDescription = item.checkboxContentDescription()
    val textColor = if (item.isPurchased) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textDecoration = if (item.isPurchased) {
        TextDecoration.LineThrough
    } else {
        TextDecoration.None
    }

    ListItem(
        headlineContent = {
            Text(
                text = item.name,
                color = textColor,
                textDecoration = textDecoration,
            )
        },
        supportingContent = item.quantityText()?.let { quantityText ->
            {
                Text(
                    text = quantityText,
                    color = textColor,
                    textDecoration = textDecoration,
                )
            }
        },
        leadingContent = {
            Checkbox(
                modifier = Modifier.semantics {
                    contentDescription = checkboxDescription
                },
                checked = item.isPurchased,
                onCheckedChange = { checked ->
                    onIntent(Intent.PurchasedChanged(item.id, checked))
                },
            )
        },
        trailingContent = {
            ItemActionsMenu(item = item, onIntent = onIntent)
        },
        tonalElevation = if (item.isPurchased) 1.dp else 0.dp,
    )
}

@Composable
private fun ItemActionsMenu(
    item: ShoppingItem,
    onIntent: (Intent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(
                    R.string.editor_item_more,
                    item.name,
                ),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_edit_item)) },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    onIntent(Intent.EditItemClicked(item))
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_delete_item)) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    onIntent(Intent.DeleteItemClicked(item))
                },
            )
        }
    }
}
