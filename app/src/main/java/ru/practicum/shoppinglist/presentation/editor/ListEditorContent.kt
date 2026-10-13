package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

private val EditorMenuLight = Color(0xFFFFF1E7)
private val EditorMenuDark = Color(0xFF211A14)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListEditorContent(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ListEditorIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showManualHandles by remember(state.listName) { mutableStateOf(false) }
    val dispatchIntent: (ListEditorIntent) -> Unit = { intent ->
        if (intent is ListEditorIntent.SortChanged) {
            showManualHandles = intent.sortType == ShoppingItemSort.MANUAL
        }
        onIntent(intent)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListsTopBar(
                listName = state.listName.ifEmpty { stringResource(R.string.editor_title) },
                onNavigateBack = onNavigateBack,
                onMenuClick = { dispatchIntent(ListEditorIntent.ToggleSortMenu) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateItemButton { dispatchIntent(ListEditorIntent.AddItemClicked) }
        },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            showManualHandles = showManualHandles,
            onIntent = dispatchIntent,
        )
    }

    EditorOverlays(
        state = state,
        onIntent = dispatchIntent,
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
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun CreateItemButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.padding(bottom = 16.dp),
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
    showManualHandles: Boolean,
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
                isManualSort = state.sortType == ShoppingItemSort.MANUAL && showManualHandles,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.figma_editor_illustration),
                contentDescription = null,
                modifier = Modifier.size(324.dp, 300.dp),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.editor_empty_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.editor_empty_subtitle),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(width = 324.dp, height = 40.dp),
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
                    onIntent(ListEditorIntent.ItemEditRequested(item.id))
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
            Alignment.CenterStart
        } else {
            Alignment.CenterEnd
        },
    ) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp),
                )
            }
            SwipeToDismissBoxValue.EndToStart -> Row {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            SwipeToDismissBoxValue.Settled -> Unit
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
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        modifier = Modifier.padding(horizontal = 6.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isSystemInDarkTheme()) EditorMenuDark else EditorMenuLight,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { EditorSheetDragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            SortSection(
                currentSortType = currentSortType,
                isExpanded = sortDropdownExpanded,
                onExpandChange = { sortDropdownExpanded = it },
                onIntent = onIntent
            )

            ContextMenuItem(
                text = stringResource(R.string.editor_delete_all),
                icon = Icons.Filled.Delete,
                iconTint = MaterialTheme.colorScheme.onBackground,
                textColor = MaterialTheme.colorScheme.onBackground,
                showDivider = true,
                onClick = {
                    onIntent(ListEditorIntent.ClearListClicked)
                    onDismiss()
                }
            )

            ContextMenuItem(
                text = stringResource(R.string.editor_clear_purchased),
                iconRes = R.drawable.figma_editor_clear,
                iconTint = MaterialTheme.colorScheme.onBackground,
                textColor = MaterialTheme.colorScheme.onBackground,
                showDivider = false,
                onClick = {
                    onIntent(ListEditorIntent.ClearCheckedClicked)
                    onDismiss()
                }
            )

        }
    }
}


@Composable
private fun SortSection(
    currentSortType: ShoppingItemSort,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onIntent: (ListEditorIntent) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        SortTriggerRow(
            currentSortType = currentSortType,
            onClick = { onExpandChange(true) }
        )

        SortDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandChange(false) },
            currentSortType = currentSortType,
            onIntent = onIntent
        )
    }
}

@Composable
private fun SortTriggerRow(
    currentSortType: ShoppingItemSort,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Text(
                        text = stringResource(R.string.sort_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = when (currentSortType) {
                            ShoppingItemSort.MANUAL -> stringResource(R.string.sort_alphabetical)
                            ShoppingItemSort.ALPHABETICAL -> stringResource(R.string.sort_manual)
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun SortMenuItem(
    sortType: ShoppingItemSort,
    currentSortType: ShoppingItemSort,
    icon: ImageVector,
    text: String,
    onIntent: (ListEditorIntent) -> Unit,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {
            onIntent(ListEditorIntent.SortChanged(sortType))
            onDismissRequest()
            }
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
        )
        Icon(
            imageVector = if (currentSortType == sortType) {
                Icons.Filled.RadioButtonChecked
            } else {
                Icons.Filled.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = if (currentSortType == sortType) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentSortType: ShoppingItemSort,
    onIntent: (ListEditorIntent) -> Unit
) {
    if (expanded) {
        val positionProvider = remember {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset = IntOffset(
                    x = (anchorBounds.right - popupContentSize.width)
                        .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
                    y = anchorBounds.top
                        .coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
                )
            }
        }
        Popup(
            popupPositionProvider = positionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            Surface(
                modifier = Modifier.size(width = 280.dp, height = 128.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                shadowElevation = 8.dp,
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SortMenuItem(
                        sortType = ShoppingItemSort.ALPHABETICAL,
                        currentSortType = currentSortType,
                        icon = Icons.Filled.SortByAlpha,
                        text = stringResource(R.string.sort_alphabetical),
                        onIntent = onIntent,
                        onDismissRequest = onDismissRequest,
                    )
                    SortMenuItem(
                        sortType = ShoppingItemSort.MANUAL,
                        currentSortType = currentSortType,
                        icon = Icons.Filled.OpenWith,
                        text = stringResource(R.string.sort_manual),
                        onIntent = onIntent,
                        onDismissRequest = onDismissRequest,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    iconTint: Color,
    textColor: Color,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(text = text, color = textColor, fontSize = 16.sp)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.outlineVariant,
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
            purchasedOnly = state.clearPurchasedOnly,
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
