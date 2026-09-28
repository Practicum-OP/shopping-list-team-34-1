package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort


private val BackgroundColor = Color(0xFFFFF8F4)
private val OnSurfaceColor = Color(0xFF211A14)
private val OnSurfaceVariantColor = Color(0xFF50453A)
private val PrimaryContainerColor = Color(0xFFFFDCBB)
private val OnPrimaryContainerColor = Color(0xFF2B1700)
private val DividerColor = Color(0xFFCAC4D0)
private val SurfaceDimColor = Color(0xFFE5D8CC)
private val PrimaryColor = Color(0xFF845416)
private val SecondaryContainerColor = Color(0xFFFEDDBD)
private val OnSecondaryContainerColor = Color(0xFF281805)
private val SortMenuBackgroundColor = Color(0xFFFAEBE0)
private val SortMenuItemSelectedColor = Color(0xFFFEDDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListEditorContent(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ListEditorIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundColor,
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

    state.createDialog?.let { dialogState ->
        CreateItemBottomSheet(
            state = dialogState,
            isSubmitting = state.isSubmitting,
            onDismiss = { onIntent(ListEditorIntent.AddItemClicked) },
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
            onDismiss = { onIntent(ListEditorIntent.ToggleSortMenu) },
            onIntent = onIntent,
        )
    }
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
                color = OnSurfaceColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = OnSurfaceColor,
                )
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.menu),
                    tint = OnSurfaceVariantColor,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundColor,
        ),
    )
}

@Composable
private fun CreateItemButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = PrimaryContainerColor,
        contentColor = OnPrimaryContainerColor,
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
            .background(BackgroundColor)
            .padding(contentPadding),
    ) {
        if (state.items.isEmpty()) {
            EmptyState()
        } else {
            ItemsList(items = state.items, onIntent = onIntent)
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
                color = Color(0xFFD4C4B5),
            ) {
                Icon(
                    modifier = Modifier.size(120.dp),
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = OnSurfaceVariantColor,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.editor_empty_title),
                    color = OnSurfaceColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp,
                )
                Text(
                    text = stringResource(R.string.editor_empty_subtitle),
                    color = OnSurfaceColor,
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
    onIntent: (ListEditorIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = items, key = ShoppingItem::id) { item ->
            SwipeableListItem(item = item, onIntent = onIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableListItem(
    item: ShoppingItem,
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
        content = { ShoppingListItem(item = item, onIntent = onIntent) },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    )
}

@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val icon: ImageVector?
    val alignment: Alignment

    when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> {
            icon = Icons.Filled.Edit
            alignment = Alignment.CenterStart
        }
        SwipeToDismissBoxValue.EndToStart -> {
            icon = Icons.Filled.Delete
            alignment = Alignment.CenterEnd
        }
        else -> {
            icon = null
            alignment = Alignment.Center
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSecondaryContainerColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ShoppingListItem(
    item: ShoppingItem,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val isPurchased = item.isPurchased
    val textColor = if (isPurchased) SurfaceDimColor else OnSurfaceColor
    val quantityColor = if (isPurchased) SurfaceDimColor else OnSurfaceVariantColor

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (isPurchased) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.item_purchased),
                    tint = PrimaryColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onIntent(ListEditorIntent.ItemToggled(item.id)) }
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = stringResource(R.string.item_not_purchased),
                    tint = OnSurfaceVariantColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onIntent(ListEditorIntent.ItemToggled(item.id)) }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textDecoration = if (isPurchased) TextDecoration.LineThrough else null,
                    maxLines = 1,
                )
                Text(
                    text = "${item.quantity} ${stringResource(item.unit?.getShortNameRes() ?: R.string.unit_piece_short)}",
                    color = quantityColor,
                    fontSize = 14.sp,
                )
            }
        }

        HorizontalDivider(
            color = DividerColor,
        )
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
        containerColor = SortMenuBackgroundColor,
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
            HorizontalDivider(color = Color(0xFFD4C4B5))
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
            .background(if (isSelected) SortMenuItemSelectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            color = OnSurfaceColor,
            fontSize = 16.sp,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
