package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

private const val PURCHASED_CONTENT_ALPHA = 0.6f

@Composable
internal fun ShoppingListItem(
    item: ShoppingItem,
    onIntent: (ListEditorIntent) -> Unit,
) {
    val purchasedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = PURCHASED_CONTENT_ALPHA,
    )

    val textColor = if (item.isPurchased) {
        purchasedColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val quantityColor = if (item.isPurchased) {
        purchasedColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.background(
            MaterialTheme.colorScheme.background,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PurchaseStatusIcon(
                isPurchased = item.isPurchased,
                onClick = {
                    onIntent(ListEditorIntent.ItemToggled(item.id))
                },
            )

            Spacer(modifier = Modifier.width(16.dp))

            ShoppingItemText(
                item = item,
                textColor = textColor,
                quantityColor = quantityColor,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
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
