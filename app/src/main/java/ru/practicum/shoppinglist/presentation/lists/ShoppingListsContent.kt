package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShoppingListsContent(
    state: ShoppingListsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (ShoppingListsAction) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ListsTopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateListButton { onAction(ShoppingListsAction.CreateListClicked) }
        },
    ) { contentPadding ->
        ListsBody(
            state = state,
            contentPadding = contentPadding,
            onAction = onAction,
        )
    }

    state.createDialog?.let { dialogState ->
        CreateListDialog(
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
private fun ListsTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.lists_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
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
                onAction = onAction,
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
    onAction: (ShoppingListsAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = shoppingLists, key = ShoppingList::id) { shoppingList ->
            ShoppingListCard(
                shoppingList = shoppingList,
                onClick = { onAction(ShoppingListsAction.ListClicked(shoppingList.id)) },
                onDelete = { onAction(ShoppingListsAction.DeleteListClicked(shoppingList)) },
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    shoppingList: ShoppingList,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val icon = ShoppingListIconRegistry.resolve(shoppingList.iconKey)
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(
                        R.string.lists_delete_content_description,
                        shoppingList.name,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
