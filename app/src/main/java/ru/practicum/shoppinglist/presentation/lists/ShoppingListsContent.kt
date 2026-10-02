package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import kotlin.math.roundToInt

private val LIST_SWIPE_ACTION_WIDTH = 72.dp
private val LIST_SWIPE_REVEAL_WIDTH = 216.dp
internal const val SHOPPING_LIST_CARD_TEST_TAG_PREFIX = "shopping_list_card_"
internal const val SHOPPING_LIST_SWIPE_TEST_TAG_PREFIX = "shopping_list_swipe_"

private enum class ListSwipeValue {
    Covered,
    Revealed,
}

private data class ListSwipeCallbacks(
    val rename: () -> Unit,
    val duplicate: () -> Unit,
    val delete: () -> Unit,
)

private data class ListSwipeDescriptions(
    val rename: String,
    val duplicate: String,
    val delete: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShoppingListsContent(
    state: ShoppingListsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (ShoppingListsAction) -> Unit,
    onItemClick: (listId: Long, listName: String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListsTopBar(
                enabled = !state.isLoggingOut && !state.isSubmitting,
                onLogout = {
                    onAction(ShoppingListsAction.LogoutClicked)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateListButton { onAction(ShoppingListsAction.CreateListClicked) }
        },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            onAction = onAction,
            onItemClick = onItemClick,
        )
    }

    state.createDialog?.let { dialogState ->
        CreateListDialog(
            state = dialogState,
            isSubmitting = state.isSubmitting,
            onAction = onAction,
        )
    }

    state.renameDialog?.let { dialogState ->
        RenameListDialog(
            state = dialogState,
            isSubmitting = state.isSubmitting,
            onAction = onAction,
        )
    }

    state.listPendingDeletion?.let { shoppingList ->
        DeleteListDialog(
            shoppingList = shoppingList,
            isSubmitting = state.isSubmitting,
            onAction = onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListsTopBar(
    enabled: Boolean,
    onLogout: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.lists_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(
                enabled = enabled,
                onClick = onLogout,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = stringResource(
                        R.string.auth_logout,
                    ),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
private fun CreateListButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(18.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.lists_create),
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun ListsBody(
    state: ShoppingListsUiState,
    contentPadding: PaddingValues,
    onAction: (ShoppingListsAction) -> Unit,
    onItemClick: (listId: Long, listName: String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )

            state.loadFailed -> ListsMessage(
                title = stringResource(R.string.lists_load_failed_title),
                body = stringResource(R.string.lists_load_failed_body),
            )

            state.lists.isEmpty() -> ListsMessage(
                title = stringResource(R.string.lists_empty_title),
                body = stringResource(R.string.lists_empty_body),
            )

            else -> ShoppingLists(
                shoppingLists = state.lists,
                actionsEnabled = !state.isSubmitting && !state.isLoggingOut,
                onAction = onAction,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun ListsMessage(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                modifier = Modifier.padding(22.dp),
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ShoppingLists(
    shoppingLists: List<ShoppingList>,
    actionsEnabled: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
    onItemClick: (listId: Long, listName: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = shoppingLists, key = ShoppingList::id) { shoppingList ->
            SwipeableShoppingListCard(
                shoppingList = shoppingList,
                onRename = { onAction(ShoppingListsAction.RenameListClicked(shoppingList)) },
                onDuplicate = {
                    onAction(ShoppingListsAction.DuplicateListClicked(shoppingList.id))
                },
                onDelete = { onAction(ShoppingListsAction.DeleteListClicked(shoppingList)) },
                onItemClick = { onItemClick(shoppingList.id, shoppingList.name) },
                enabled = actionsEnabled,
            )
        }
    }
}

@Composable
private fun SwipeableShoppingListCard(
    shoppingList: ShoppingList,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onItemClick: () -> Unit,
    enabled: Boolean,
) {
    val swipeState = rememberListSwipeState()
    val coroutineScope = rememberCoroutineScope()
    val descriptions = listSwipeDescriptions(shoppingList.name)
    val callbacks = ListSwipeCallbacks(
        rename = { coroutineScope.closeSwipeAndRun(swipeState, enabled, onRename) },
        duplicate = { coroutineScope.closeSwipeAndRun(swipeState, enabled, onDuplicate) },
        delete = { coroutineScope.closeSwipeAndRun(swipeState, enabled, onDelete) },
    )

    LaunchedEffect(enabled) {
        if (!enabled) {
            swipeState.animateTo(ListSwipeValue.Covered)
        }
    }

    ShoppingListSwipeLayout(
        shoppingList = shoppingList,
        swipeState = swipeState,
        descriptions = descriptions,
        callbacks = callbacks,
        enabled = enabled,
        onItemClick = {
            coroutineScope.handleListCardClick(swipeState, onItemClick)
        },
    )
}

@Composable
private fun ShoppingListSwipeLayout(
    shoppingList: ShoppingList,
    swipeState: AnchoredDraggableState<ListSwipeValue>,
    descriptions: ListSwipeDescriptions,
    callbacks: ListSwipeCallbacks,
    enabled: Boolean,
    onItemClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SHOPPING_LIST_SWIPE_TEST_TAG_PREFIX + shoppingList.id)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            ListSwipeActions(
                descriptions = descriptions,
                callbacks = callbacks,
            )
        }
        ShoppingListCard(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = swipeState.requireOffset().roundToInt(),
                        y = 0,
                    )
                }
                .testTag(SHOPPING_LIST_CARD_TEST_TAG_PREFIX + shoppingList.id)
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                )
                .listSwipeSemantics(enabled, descriptions, callbacks),
            shoppingList = shoppingList,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun rememberListSwipeState(): AnchoredDraggableState<ListSwipeValue> {
    val revealDistancePx = with(LocalDensity.current) {
        LIST_SWIPE_REVEAL_WIDTH.toPx()
    }
    val revealOffset = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> -revealDistancePx
        LayoutDirection.Rtl -> revealDistancePx
    }
    return remember(revealOffset) {
        AnchoredDraggableState(
            initialValue = ListSwipeValue.Covered,
            anchors = DraggableAnchors {
                ListSwipeValue.Covered at 0f
                ListSwipeValue.Revealed at revealOffset
            },
        )
    }
}

@Composable
private fun listSwipeDescriptions(listName: String) = ListSwipeDescriptions(
    rename = stringResource(R.string.lists_rename_content_description, listName),
    duplicate = stringResource(R.string.lists_duplicate_content_description, listName),
    delete = stringResource(R.string.lists_delete_content_description, listName),
)

private fun CoroutineScope.closeSwipeAndRun(
    swipeState: AnchoredDraggableState<ListSwipeValue>,
    enabled: Boolean,
    action: () -> Unit,
) {
    if (!enabled) return
    launch {
        swipeState.animateTo(ListSwipeValue.Covered)
        action()
    }
}

private fun CoroutineScope.handleListCardClick(
    swipeState: AnchoredDraggableState<ListSwipeValue>,
    onItemClick: () -> Unit,
) {
    if (swipeState.settledValue == ListSwipeValue.Revealed) {
        launch { swipeState.animateTo(ListSwipeValue.Covered) }
    } else {
        onItemClick()
    }
}

private fun Modifier.listSwipeSemantics(
    enabled: Boolean,
    descriptions: ListSwipeDescriptions,
    callbacks: ListSwipeCallbacks,
): Modifier = semantics {
    customActions = if (enabled) {
        listOf(
            CustomAccessibilityAction(descriptions.rename) {
                callbacks.rename()
                true
            },
            CustomAccessibilityAction(descriptions.duplicate) {
                callbacks.duplicate()
                true
            },
            CustomAccessibilityAction(descriptions.delete) {
                callbacks.delete()
                true
            },
        )
    } else {
        emptyList()
    }
}

@Composable
private fun ShoppingListCard(
    modifier: Modifier = Modifier,
    shoppingList: ShoppingList,
    onItemClick: () -> Unit,
) {
    val icon = ShoppingListIconRegistry.resolve(shoppingList.iconKey)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp),
                imageVector = icon.imageVector,
                contentDescription = stringResource(icon.labelRes),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = shoppingList.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ListSwipeActions(
    descriptions: ListSwipeDescriptions,
    callbacks: ListSwipeCallbacks,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        horizontalArrangement = Arrangement.End,
    ) {
        SwipeAction(
            icon = Icons.Outlined.Edit,
            contentDescription = descriptions.rename,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = callbacks.rename,
        )
        SwipeAction(
            icon = Icons.Outlined.ContentCopy,
            contentDescription = descriptions.duplicate,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onClick = callbacks.duplicate,
        )
        SwipeAction(
            icon = Icons.Outlined.Delete,
            contentDescription = descriptions.delete,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            onClick = callbacks.delete,
        )
    }
}

@Composable
private fun SwipeAction(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(LIST_SWIPE_ACTION_WIDTH)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
        )
    }
}
