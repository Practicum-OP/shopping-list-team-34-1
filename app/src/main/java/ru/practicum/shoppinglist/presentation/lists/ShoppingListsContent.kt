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
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import kotlin.math.roundToInt

private val LIST_SWIPE_ICON_SIZE = 40.dp
private val LIST_SWIPE_ACTION_GAP = 8.dp
private val LIST_SWIPE_END_PADDING = 16.dp
private val LIST_SWIPE_ITEM_GAP = 16.dp
private val LIST_SWIPE_REVEAL_WIDTH = LIST_SWIPE_ICON_SIZE * 3 +
        LIST_SWIPE_ACTION_GAP * 2 + LIST_SWIPE_END_PADDING + LIST_SWIPE_ITEM_GAP
internal const val SHOPPING_LIST_CARD_TEST_TAG_PREFIX = "shopping_list_card_"
internal const val SHOPPING_LIST_SWIPE_TEST_TAG_PREFIX = "shopping_list_swipe_"
private val LIST_SWIPE_ICON_INNER_SIZE = 24.dp

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
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (searchOpen) {
                ListsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        searchQuery = ""
                        searchOpen = false
                    },
                )
            } else {
                ListsTopBar(
                    enabled = !state.isLoggingOut && !state.isSubmitting,
                    isDarkTheme = isDarkTheme,
                    onSearch = { searchOpen = true },
                    onDeleteAll = { onAction(ShoppingListsAction.DeleteAllListsClicked) },
                    onToggleTheme = onToggleTheme,
                    onLogout = { onAction(ShoppingListsAction.LogoutClicked) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!searchOpen) {
                CreateListButton { onAction(ShoppingListsAction.CreateListClicked) }
            }
        },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            searchQuery = if (searchOpen) searchQuery else null,
            onDismissSearch = {
                searchQuery = ""
                searchOpen = false
            },
            onAction = onAction,
            onItemClick = onItemClick,
        )
    }

    ListsDialogs(state = state, onAction = onAction)
}

@Composable
private fun ListsDialogs(
    state: ShoppingListsUiState,
    onAction: (ShoppingListsAction) -> Unit,
) {
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

    if (state.deleteAllListsRequested) {
        DeleteAllListsDialog(
            isSubmitting = state.isSubmitting,
            onAction = onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListsTopBar(
    enabled: Boolean,
    isDarkTheme: Boolean,
    onSearch: () -> Unit,
    onDeleteAll: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.lists_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
            )
        },
        actions = {
            IconButton(enabled = enabled, onClick = onSearch) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.lists_search),
                )
            }
            IconButton(enabled = enabled, onClick = onDeleteAll) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.lists_delete_all),
                )
            }
            IconButton(enabled = enabled, onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = stringResource(R.string.lists_toggle_theme),
                )
            }
            ListsOverflowMenu(enabled = enabled, onLogout = onLogout)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun ListsOverflowMenu(enabled: Boolean, onLogout: () -> Unit) {
    var overflowOpen by remember { mutableStateOf(false) }
    Box {
        IconButton(enabled = enabled, onClick = { overflowOpen = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.lists_more_actions),
            )
        }
        DropdownMenu(
            expanded = overflowOpen,
            onDismissRequest = { overflowOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.auth_logout)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                },
                onClick = {
                    overflowOpen = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun ListsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(modifier = Modifier.fillMaxWidth().height(73.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                }
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.lists_search_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.lists_clear_search))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateListButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.lists_create),
        )
    }
}

@Composable
private fun ListsBody(
    state: ShoppingListsUiState,
    contentPadding: PaddingValues,
    searchQuery: String?,
    onDismissSearch: () -> Unit,
    onAction: (ShoppingListsAction) -> Unit,
    onItemClick: (listId: Long, listName: String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        val visibleLists = if (searchQuery.isNullOrBlank()) state.lists else {
            state.lists.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        val searchHasNoResults = searchQuery != null && searchQuery.isNotBlank() && visibleLists.isEmpty()
        ListsBodyBackground(searchQuery, searchHasNoResults, visibleLists.isNotEmpty())
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )

            state.loadFailed -> ListsMessage(
                title = stringResource(R.string.lists_load_failed_title),
                body = stringResource(R.string.lists_load_failed_body),
            )

            searchHasNoResults -> SearchNoResults(
                title = stringResource(R.string.lists_search_empty_title),
                body = stringResource(R.string.lists_search_empty_body),
            )

            state.lists.isEmpty() -> ListsMessage(
                title = stringResource(R.string.lists_empty_title),
                body = stringResource(R.string.lists_empty_body),
            )

            else -> ShoppingLists(
                shoppingLists = visibleLists,
                actionsEnabled = !state.isSubmitting && !state.isLoggingOut,
                searchResults = searchQuery != null && searchQuery.isNotBlank(),
                onAction = onAction,
                onItemClick = onItemClick,
            )
        }
        if (searchQuery != null && searchQuery.isEmpty()) {
            SearchScrim(onDismissSearch)
        }
    }
}

@Composable
private fun BoxScope.ListsBodyBackground(
    searchQuery: String?,
    searchHasNoResults: Boolean,
    hasVisibleLists: Boolean,
) {
    val isSearching = searchQuery != null && searchQuery.isNotBlank()
    val bodyColor = when {
        searchHasNoResults -> MaterialTheme.colorScheme.surfaceContainerHigh
        isSearching -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.background
    }
    Box(Modifier.matchParentSize().background(bodyColor))
    if (isSearching && hasVisibleLists) {
        Box(Modifier.fillMaxWidth().height(61.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh))
    }
}

@Composable
private fun SearchScrim(onDismissSearch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(onClick = onDismissSearch),
    )
}

@Composable
private fun BoxScope.ListsMessage(title: String, body: String) {
    Column(
        modifier = Modifier
            .width(324.dp)
            .height(420.dp)
            .offset(y = (-46).dp)
            .align(Alignment.Center),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.figma_lists_illustration),
            contentDescription = null,
            modifier = Modifier.size(width = 324.dp, height = 300.dp),
        )
        Spacer(Modifier.height(48.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BoxScope.SearchNoResults(title: String, body: String) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 31.dp)
            .width(284.dp)
            .height(366.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.figma_lists_search_empty),
            contentDescription = null,
            modifier = Modifier.size(width = 284.dp, height = 238.dp),
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ShoppingLists(
    shoppingLists: List<ShoppingList>,
    actionsEnabled: Boolean,
    searchResults: Boolean,
    onAction: (ShoppingListsAction) -> Unit,
    onItemClick: (listId: Long, listName: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = if (searchResults) 61.dp else 16.dp,
            end = 16.dp,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
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
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                imageVector = icon.imageVector,
                contentDescription = stringResource(icon.labelRes),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = shoppingList.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
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
            .height(LIST_SWIPE_ICON_SIZE)
            .padding(end = LIST_SWIPE_END_PADDING),
        horizontalArrangement = Arrangement.spacedBy(LIST_SWIPE_ACTION_GAP),
    ) {
        SwipeAction(
            icon = Icons.Rounded.Edit,
            contentDescription = descriptions.rename,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = callbacks.rename,
        )
        SwipeAction(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = descriptions.duplicate,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = callbacks.duplicate,
        )
        SwipeAction(
            icon = Icons.Rounded.Delete,
            contentDescription = descriptions.delete,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
            .size(LIST_SWIPE_ICON_SIZE)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(LIST_SWIPE_ICON_INNER_SIZE),
        )
    }
}
