package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

private const val PURCHASED_CONTENT_ALPHA = 0.6f

private data class ItemDragInfo(
    val index: Int,
    val itemCount: Int,
    val isEnabled: Boolean,
)

private data class ShoppingItemColors(
    val text: Color,
    val quantity: Color,
    val background: Color,
)

private data class DragDescriptions(
    val handle: String,
    val moveUp: String,
    val moveDown: String,
)

@Composable
internal fun ShoppingListItem(
    item: ShoppingItem,
    itemIndex: Int,
    itemCount: Int,
    isDragEnabled: Boolean,
    onIntent: (ListEditorIntent) -> Unit,
) {
    var isDragging by remember(item.id) { mutableStateOf(false) }
    val colors = shoppingItemColors(item.isPurchased, isDragging)

    Column(
        modifier = Modifier.background(colors.background),
    ) {
        ShoppingItemRow(
            item = item,
            dragInfo = ItemDragInfo(itemIndex, itemCount, isDragEnabled),
            colors = colors,
            onIntent = onIntent,
            onDraggingChanged = { isDragging = it },
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    dragInfo: ItemDragInfo,
    colors: ShoppingItemColors,
    onIntent: (ListEditorIntent) -> Unit,
    onDraggingChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_ROW_HEIGHT)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PurchaseStatusIcon(
            isPurchased = item.isPurchased,
            onClick = { onIntent(ListEditorIntent.ItemToggled(item.id)) },
        )
        Spacer(modifier = Modifier.width(16.dp))
        ShoppingItemText(
            item = item,
            textColor = colors.text,
            quantityColor = colors.quantity,
            modifier = Modifier.weight(1f),
        )
        if (dragInfo.isEnabled) {
            DragHandle(item, dragInfo, onIntent, onDraggingChanged)
        }
    }
}

@Composable
private fun DragHandle(
    item: ShoppingItem,
    dragInfo: ItemDragInfo,
    onIntent: (ListEditorIntent) -> Unit,
    onDraggingChanged: (Boolean) -> Unit,
) {
    val state = remember(item.id) { ItemDragState(item.id) }
    state.update(dragInfo, onIntent, onDraggingChanged)
    val descriptions = DragDescriptions(
        handle = stringResource(R.string.drag_handle_item, item.name),
        moveUp = stringResource(R.string.drag_move_up),
        moveDown = stringResource(R.string.drag_move_down),
    )
    val dragStep = with(LocalDensity.current) { ITEM_ROW_HEIGHT.toPx() }
    Box(
        modifier = Modifier
            .size(DRAG_TOUCH_TARGET_SIZE)
            .testTag("drag_handle_${item.id}")
            .itemDragGesture(state, LocalHapticFeedback.current, dragStep)
            .itemDragSemantics(state, descriptions),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.DragHandle,
            contentDescription = null,
            tint = if (state.isDragging) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(DRAG_ICON_SIZE),
        )
    }
}

private fun Modifier.itemDragGesture(
    state: ItemDragState,
    hapticFeedback: HapticFeedback,
    dragStep: Float,
): Modifier = pointerInput(state.itemId) {
    detectDragGesturesAfterLongPress(
        onDragStart = {
            state.startDragging()
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onDragCancel = state::stopDragging,
        onDragEnd = state::stopDragging,
        onDrag = { change, dragAmount ->
            change.consume()
            state.dragBy(dragAmount.y, dragStep)
        },
    )
}

private fun Modifier.itemDragSemantics(
    state: ItemDragState,
    descriptions: DragDescriptions,
): Modifier = semantics {
    contentDescription = descriptions.handle
    customActions = buildList {
        if (state.canMoveUp) {
            add(CustomAccessibilityAction(descriptions.moveUp) {
                state.move(MOVE_UP)
                true
            })
        }
        if (state.canMoveDown) {
            add(CustomAccessibilityAction(descriptions.moveDown) {
                state.move(MOVE_DOWN)
                true
            })
        }
    }
}

private class ItemDragState(
    val itemId: Long,
) {
    private var info = ItemDragInfo(0, 0, false)
    private var onIntent: (ListEditorIntent) -> Unit = {}
    private var onDraggingChanged: (Boolean) -> Unit = {}
    private var draggedDistance by mutableFloatStateOf(0f)
    var isDragging by mutableStateOf(false)
        private set

    val canMoveUp: Boolean get() = info.index > 0
    val canMoveDown: Boolean get() = info.index < info.itemCount - 1

    fun update(
        info: ItemDragInfo,
        onIntent: (ListEditorIntent) -> Unit,
        onDraggingChanged: (Boolean) -> Unit,
    ) {
        this.info = info
        this.onIntent = onIntent
        this.onDraggingChanged = onDraggingChanged
    }

    fun startDragging() {
        draggedDistance = 0f
        isDragging = true
        onDraggingChanged(true)
    }

    fun stopDragging() {
        draggedDistance = 0f
        isDragging = false
        onDraggingChanged(false)
    }

    fun dragBy(delta: Float, dragStep: Float) {
        draggedDistance += delta
        while (draggedDistance >= dragStep) {
            move(MOVE_DOWN)
            draggedDistance -= dragStep
        }
        while (draggedDistance <= -dragStep) {
            move(MOVE_UP)
            draggedDistance += dragStep
        }
    }

    fun move(direction: Int) {
        onIntent(ListEditorIntent.ItemMoved(itemId, direction))
    }
}

@Composable
private fun shoppingItemColors(
    isPurchased: Boolean,
    isDragging: Boolean,
): ShoppingItemColors {
    val purchasedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = PURCHASED_CONTENT_ALPHA,
    )
    return ShoppingItemColors(
        text = if (isPurchased) purchasedColor else MaterialTheme.colorScheme.onSurface,
        quantity = if (isPurchased) {
            purchasedColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        background = if (isDragging) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.background
        },
    )
}

@Composable
private fun PurchaseStatusIcon(
    isPurchased: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isPurchased) {
        Icons.Filled.CheckCircle
    } else {
        Icons.Filled.Circle
    }

    val description = if (isPurchased) {
        stringResource(R.string.item_purchased)
    } else {
        stringResource(R.string.item_not_purchased)
    }

    val tint = if (isPurchased) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ShoppingItemText(
    item: ShoppingItem,
    textColor: Color,
    quantityColor: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.name,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textDecoration = if (item.isPurchased) {
                TextDecoration.LineThrough
            } else {
                null
            },
            maxLines = 1,
        )

        Text(
            text = "${item.quantity} ${
                stringResource(
                    item.unit?.getShortNameRes()
                        ?: R.string.unit_piece_short,
                )
            }",
            color = quantityColor,
            fontSize = 14.sp,
        )
    }
}

private val ITEM_ROW_HEIGHT = 72.dp
private val DRAG_TOUCH_TARGET_SIZE = 48.dp
private val DRAG_ICON_SIZE = 24.dp
private const val MOVE_UP = -1
private const val MOVE_DOWN = 1
