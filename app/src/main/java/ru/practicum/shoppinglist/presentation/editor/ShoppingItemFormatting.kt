package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

@Composable
internal fun ShoppingItem.quantityText(): String? {
    if (quantity == null && unit == null) return null

    val quantityText = quantity?.let { value ->
        if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString().replace('.', ',')
        }
    }
    val unitText = unit?.displayName()
    return listOfNotNull(quantityText, unitText).joinToString(separator = " ")
}

@Composable
internal fun MeasurementUnit.displayName(): String = stringResource(
    when (this) {
        MeasurementUnit.PIECE -> R.string.editor_unit_piece
        MeasurementUnit.KILOGRAM -> R.string.editor_unit_kilogram
        MeasurementUnit.LITER -> R.string.editor_unit_liter
        MeasurementUnit.MILLILITER -> R.string.editor_unit_milliliter
        MeasurementUnit.GRAM -> R.string.editor_unit_gram
    },
)

@Composable
internal fun ShoppingItem.checkboxContentDescription(): String = stringResource(
    if (isPurchased) {
        R.string.editor_mark_not_purchased
    } else {
        R.string.editor_mark_purchased
    },
    name,
)
