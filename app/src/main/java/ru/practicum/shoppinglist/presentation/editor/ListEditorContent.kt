package ru.practicum.shoppinglist.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import kotlin.math.roundToInt


private val FAB_SIZE = 56.dp
private val FAB_END_PADDING = 16.dp
private val FAB_CLOSED_BOTTOM = 56.dp
private val FAB_OPEN_GAP = 32.dp
private const val CONFIRM_ITEM_TEST_TAG = "confirm_item_fab"

private const val SHEET_ANIM_DURATION_MS = 300
private const val SHEET_SCRIM_ALPHA = 0.32f
private const val SHEET_SCRIM_ALPHA_THRESHOLD = 0.001f
private const val SHEET_DRAG_THRESHOLD = 0.25f
private const val SHEET_DISMISS_VELOCITY = 1000f
private const val FAB_ICON_ANIM_DURATION_MS = 200
private const val FAB_ICON_SCALE = 0.5f
private val ITEM_ACTION_SIZE = 40.dp
private val ITEM_ACTION_GAP = 8.dp
private val ITEM_ACTION_ICON_SIZE = 24.dp
private val ITEM_ACTIONS_END_PADDING = 16.dp
private val ITEM_ACTIONS_START_PADDING = 16.dp
private val ITEM_ACTIONS_WIDTH = ITEM_ACTION_SIZE * 2 + ITEM_ACTION_GAP +
        ITEM_ACTIONS_START_PADDING + ITEM_ACTIONS_END_PADDING
private const val ITEM_SWIPE_ANIM_MS = 200
private const val ITEM_SWIPE_SNAP_VELOCITY = 800f
private const val ITEM_SWIPE_OPEN_THRESHOLD = 0.5f

@Composable
internal fun ListEditorContent(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ListEditorIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showManualHandles by remember(state.listName) { mutableStateOf(false) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val dispatchIntent: (ListEditorIntent) -> Unit = { intent ->
        if (intent is ListEditorIntent.SortChanged) {
            showManualHandles = intent.sortType == ShoppingItemSort.MANUAL
        }
        onIntent(intent)
    }
    EditorScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        showManualHandles = showManualHandles,
        sheetHeightPx = sheetHeightPx,
        onSheetHeightChanged = { sheetHeightPx = it },
        onNavigateBack = onNavigateBack,
        onIntent = dispatchIntent,
    )
}

@Composable
private fun EditorScreen(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    showManualHandles: Boolean,
    sheetHeightPx: Int,
    onSheetHeightChanged: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val editorOpen = state.createDialog != null
    Box(modifier = Modifier.fillMaxSize()) {
        EditorScaffold(
            state = state,
            snackbarHostState = snackbarHostState,
            showManualHandles = showManualHandles,
            onNavigateBack = onNavigateBack,
            onIntent = onIntent,
        )
        EditorSheetHost(
            state = state,
            sheetHeightPx = sheetHeightPx,
            onHeightChanged = onSheetHeightChanged,
            onDismiss = { onIntent(ListEditorIntent.AddItemClicked) },
            onIntent = onIntent,
        )
        EditorFabHost(
            editorOpen = editorOpen,
            isSubmitting = state.isSubmitting,
            sheetHeightPx = sheetHeightPx,
            onClick = {
                if (editorOpen) onIntent(ListEditorIntent.ConfirmAddItem)
                else onIntent(ListEditorIntent.AddItemClicked)
            },
        )
    }
    EditorOverlays(state = state, onIntent = onIntent)
}

@Composable
private fun EditorScaffold(
    state: ListEditorUiState,
    snackbarHostState: SnackbarHostState,
    showManualHandles: Boolean,
    onNavigateBack: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListsTopBar(
                listName = state.listName.ifEmpty { stringResource(R.string.editor_title) },
                onNavigateBack = onNavigateBack,
                onMenuClick = { onIntent(ListEditorIntent.ToggleSortMenu) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            showManualHandles = showManualHandles,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun BoxScope.EditorSheetHost(
    state: ListEditorUiState,
    sheetHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    EditorSheet(
        visible = state.createDialog != null,
        sheetHeightPx = sheetHeightPx,
        onHeightChanged = onHeightChanged,
        onDismiss = onDismiss,
    ) {
        state.createDialog?.let { dialogState ->
            CreateItemSheetContent(
                state = dialogState,
                isSubmitting = state.isSubmitting,
                onIntent = onIntent,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun BoxScope.EditorFabHost(
    editorOpen: Boolean,
    isSubmitting: Boolean,
    sheetHeightPx: Int,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val targetShift = if (editorOpen) {
        with(density) { sheetHeightPx.toDp() } + FAB_OPEN_GAP
    } else {
        0.dp
    }
    val shift by animateDpAsState(
        targetValue = targetShift,
        animationSpec = tween(SHEET_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "fabShift",
    )
    EditorFab(
        isEditorOpen = editorOpen,
        isSubmitting = isSubmitting,
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = FAB_END_PADDING, bottom = FAB_CLOSED_BOTTOM)
            .offset(y = -shift),
    )
}

@Composable
private fun EditorFab(
    isEditorOpen: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(FAB_SIZE)
            .testTag(CONFIRM_ITEM_TEST_TAG),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        FabContent(isEditorOpen = isEditorOpen, isSubmitting = isSubmitting)
    }
}

@Composable
private fun FabContent(isEditorOpen: Boolean, isSubmitting: Boolean) {
    if (isSubmitting) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        AnimatedContent(
            targetState = isEditorOpen,
            transitionSpec = {
                (fadeIn(tween(FAB_ICON_ANIM_DURATION_MS)) +
                        scaleIn(initialScale = FAB_ICON_SCALE)) togetherWith
                        (fadeOut(tween(FAB_ICON_ANIM_DURATION_MS)) +
                                scaleOut(targetScale = FAB_ICON_SCALE))
            },
            label = "fabIcon",
        ) { open ->
            Icon(
                imageVector = if (open) Icons.Filled.Check else Icons.Outlined.Add,
                contentDescription = stringResource(
                    if (open) R.string.action_save else R.string.editor_create,
                ),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun BoxScope.EditorSheet(
    visible: Boolean,
    sheetHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else sheetHeightPx.toFloat(),
        animationSpec = tween(SHEET_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "sheetOffset",
    )
    val scrim by animateFloatAsState(
        targetValue = if (visible) SHEET_SCRIM_ALPHA else 0f,
        animationSpec = tween(SHEET_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "sheetScrim",
    )
    BackHandler(enabled = visible, onBack = onDismiss)
    if (scrim > SHEET_SCRIM_ALPHA_THRESHOLD) SheetScrim(alpha = scrim, onDismiss = onDismiss)
    SheetSurface(
        offset = offset,
        sheetHeightPx = sheetHeightPx,
        onHeightChanged = onHeightChanged,
        onDismiss = onDismiss,
        content = content,
    )
}

@Composable
private fun BoxScope.SheetScrim(alpha: Float, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    )
}

@Composable
private fun BoxScope.SheetSurface(
    offset: Float,
    sheetHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .offset { IntOffset(0, (offset + dragOffset).roundToInt()) }
            .onGloballyPositioned { onHeightChanged(it.size.height) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = dragState,
                        onDragStopped = { velocity ->
                            val beyondThreshold =
                                dragOffset > sheetHeightPx * SHEET_DRAG_THRESHOLD
                            val fastEnough = velocity > SHEET_DISMISS_VELOCITY
                            if (beyondThreshold || fastEnough) {
                                onDismiss()
                            }
                            dragOffset = 0f
                        },
                    ),
            ) {
                EditorSheetDragHandle()
            }
            content()
        }
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
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(width = 324.dp, height = 40.dp),
                )
            }
        }
    }
}

@Composable
private fun ItemsList(
    items: List<ShoppingItem>,
    isManualSort: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
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

@Composable
private fun SwipeableListItem(
    item: ShoppingItem,
    itemIndex: Int,
    itemCount: Int,
    isManualSort: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val density = LocalDensity.current
    val actionsWidthPx = with(density) { ITEM_ACTIONS_WIDTH.toPx() }
    var offsetX by remember(item.id) { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(ITEM_SWIPE_ANIM_MS),
        label = "itemOffset",
    )
    val dragState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-actionsWidthPx, 0f)
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        ItemActionsRow(
            onEdit = {
                offsetX = 0f
                onIntent(ListEditorIntent.ItemEditRequested(item.id))
            },
            onDelete = {
                offsetX = 0f
                onIntent(ListEditorIntent.DeleteItemRequested(item.id))
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = dragState,
                    onDragStopped = { velocity ->
                        offsetX = resolveItemSnap(offsetX, velocity, actionsWidthPx)
                    },
                ),
        ) {
            ShoppingListItem(
                item = item,
                itemIndex = itemIndex,
                itemCount = itemCount,
                isDragEnabled = isManualSort,
                onIntent = onIntent,
            )
        }
    }
}

private fun resolveItemSnap(
    current: Float,
    velocity: Float,
    actionsWidthPx: Float,
): Float = when {
    velocity < -ITEM_SWIPE_SNAP_VELOCITY -> -actionsWidthPx
    velocity > ITEM_SWIPE_SNAP_VELOCITY -> 0f
    current < -actionsWidthPx * ITEM_SWIPE_OPEN_THRESHOLD -> -actionsWidthPx
    else -> 0f
}

@Composable
private fun BoxScope.ItemActionsRow(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .matchParentSize()
            .padding(end = ITEM_ACTIONS_END_PADDING),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwipeActionButton(
            icon = Icons.Filled.Edit,
            contentDescription = null,
            onClick = onEdit,
        )
        Spacer(modifier = Modifier.size(ITEM_ACTION_GAP))
        SwipeActionButton(
            icon = Icons.Filled.Delete,
            contentDescription = null,
            onClick = onDelete,
        )
    }
}

@Composable
private fun SwipeActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(ITEM_ACTION_SIZE),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(ITEM_ACTION_ICON_SIZE),
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
    var sortExpanded by remember { mutableStateOf(false) }
    ModalBottomSheet(
        modifier = Modifier.padding(horizontal = 6.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { EditorSheetDragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SortSection(
                currentSortType = currentSortType,
                isExpanded = sortExpanded,
                onExpandChange = { sortExpanded = it },
                onIntent = onIntent,
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
                },
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
                },
            )
        }
    }
}

@Composable
private fun SortSection(
    currentSortType: ShoppingItemSort,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onIntent: (ListEditorIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        SortTriggerRow(
            currentSortType = currentSortType,
            onClick = { onExpandChange(true) },
        )
        SortDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandChange(false) },
            currentSortType = currentSortType,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun SortTriggerRow(
    currentSortType: ShoppingItemSort,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SortTriggerLabel(currentSortType)
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
private fun SortTriggerLabel(currentSortType: ShoppingItemSort) {
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
                text = stringResource(currentSortType.titleRes()),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
            )
        }
    }
}

private fun ShoppingItemSort.titleRes(): Int = when (this) {
    ShoppingItemSort.MANUAL -> R.string.sort_manual
    ShoppingItemSort.ALPHABETICAL -> R.string.sort_alphabetical
}

@Composable
private fun SortMenuItem(
    sortType: ShoppingItemSort,
    currentSortType: ShoppingItemSort,
    icon: ImageVector,
    text: String,
    onIntent: (ListEditorIntent) -> Unit,
    onDismissRequest: () -> Unit,
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
        SortRadio(current = currentSortType == sortType)
    }
}

@Composable
private fun SortRadio(current: Boolean) {
    Icon(
        imageVector = if (current) {
            Icons.Filled.RadioButtonChecked
        } else {
            Icons.Filled.RadioButtonUnchecked
        },
        contentDescription = null,
        tint = if (current) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentSortType: ShoppingItemSort,
    onIntent: (ListEditorIntent) -> Unit,
) {
    if (!expanded) return
    val positionProvider = remember { editorPopupPositionProvider() }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        SortDropdownSurface(currentSortType, onIntent, onDismissRequest)
    }
}

@Composable
private fun SortDropdownSurface(
    currentSortType: ShoppingItemSort,
    onIntent: (ListEditorIntent) -> Unit,
    onDismissRequest: () -> Unit,
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

private fun editorPopupPositionProvider(): PopupPositionProvider = object : PopupPositionProvider {
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
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)) {
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
    state.listPendingDeletion?.let { item ->
        DeleteItemDialog(
            shoppingItem = item,
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
            onDismiss = { onIntent(ListEditorIntent.ToggleSortMenu) },
            onIntent = onIntent,
        )
    }
}
