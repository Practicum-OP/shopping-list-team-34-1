package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListEditorContent(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ListEditorIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListsTopBar(
                listName = state.listName.ifEmpty { stringResource(R.string.editor_title) },
                onNavigateBack = onNavigateBack,
                onMenuClick = { onIntent(ListEditorIntent.ToggleSortMenu) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateItemButton { onIntent(ListEditorIntent.AddItemClicked) }
        },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            onIntent = onIntent,
        )
    }

    EditorOverlays(
        state = state,
        onIntent = onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListsTopBar(
    listName: String,
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = listName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
private fun CreateItemButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.editor_create),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ListsBody(
    state: ListEditorUiState,
    contentPadding: PaddingValues,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
    ) {
        if (state.items.isEmpty()) {
            EmptyState()
        } else {
            ItemsList(
                items = state.items,
                isManualSort = state.sortType == ShoppingItemSort.MANUAL,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Surface(
                modifier = Modifier.size(324.dp, 300.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    modifier = Modifier.size(120.dp),
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.editor_empty_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp,
                )
                Text(
                    text = stringResource(R.string.editor_empty_subtitle),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsList(
    items: List<ShoppingItem>,
    isManualSort: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id },
        ) { index, item ->
            SwipeableListItem(
                item = item,
                itemIndex = index,
                itemCount = items.size,
                isManualSort = isManualSort,
                onIntent = onIntent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableListItem(
    item: ShoppingItem,
    itemIndex: Int,
    itemCount: Int,
    isManualSort: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Свайп вправо - редактирование
                    onIntent(ListEditorIntent.ItemEditRequested(item.id))
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Свайп влево - удаление
                    onIntent(ListEditorIntent.DeleteItemRequested(item.id))
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground(dismissState) },
        content = {
            ShoppingListItem(
                item = item,
                itemIndex = itemIndex,
                itemCount = itemCount,
                isDragEnabled = isManualSort,
                onIntent = onIntent,
            )
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    )
}

@Composable
private fun SwipeBackground(
    dismissState: SwipeToDismissBoxState,
) {
    val direction = dismissState.dismissDirection

    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Edit
        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
        SwipeToDismissBoxValue.Settled -> null
    }

    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    val backgroundColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd ->
            MaterialTheme.colorScheme.primaryContainer

        SwipeToDismissBoxValue.EndToStart ->
            MaterialTheme.colorScheme.errorContainer

        SwipeToDismissBoxValue.Settled ->
            MaterialTheme.colorScheme.background
    }

    val iconColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd ->
            MaterialTheme.colorScheme.onPrimaryContainer

        SwipeToDismissBoxValue.EndToStart ->
            MaterialTheme.colorScheme.onErrorContainer

        SwipeToDismissBoxValue.Settled ->
            MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenuBottomSheet(
    currentSortType: ShoppingItemSort,
    onDismiss: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SortMenuItem(
                text = stringResource(R.string.sort_manual),
                isSelected = currentSortType == ShoppingItemSort.MANUAL,
                onClick = {
                    onIntent(ListEditorIntent.SortChanged(ShoppingItemSort.MANUAL))
                    onDismiss()
                }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SortMenuItem(
                text = stringResource(R.string.sort_alphabetical),
                isSelected = currentSortType == ShoppingItemSort.ALPHABETICAL,
                onClick = {
                    onIntent(ListEditorIntent.SortChanged(ShoppingItemSort.ALPHABETICAL))
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}

@Composable
private fun SortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
private fun EditorOverlays(
    state: ListEditorUiState,
    onIntent: (ListEditorIntent) -> Unit,
) {
    state.createDialog?.let { dialogState ->
        CreateItemBottomSheet(
            state = dialogState,
            isSubmitting = state.isSubmitting,
            onDismiss = {
                onIntent(ListEditorIntent.AddItemClicked)
            },
            onIntent = onIntent,
            isEditing = dialogState.isEditing,
        )
    }

    state.listPendingDeletion?.let { shoppingItem ->
        DeleteItemDialog(
            shoppingItem = shoppingItem,
            isSubmitting = state.isSubmitting,
            onIntent = onIntent,
        )
    }

    if (state.showClearDialog) {
        ClearListDialog(
            isSubmitting = state.isSubmitting,
            onIntent = onIntent,
        )
    }

    if (state.showSortMenu) {
        SortMenuBottomSheet(
            currentSortType = state.sortType,
            onDismiss = {
                onIntent(ListEditorIntent.ToggleSortMenu)
            },
            onIntent = onIntent,
        )
    }
}
