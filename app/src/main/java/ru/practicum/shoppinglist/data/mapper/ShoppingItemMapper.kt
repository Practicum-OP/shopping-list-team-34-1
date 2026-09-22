package ru.practicum.shoppinglist.data.mapper

import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem

internal fun ShoppingItemEntity.toDomain(): ShoppingItem =
    ShoppingItem(
        id = id,
        shoppingListId = shoppingListId,
        name = name,
        quantity = quantity,
        unit = unit.toMeasurementUnit(),
        isPurchased = isPurchased,
        position = position,
    )

internal fun ShoppingItem.toEntity(): ShoppingItemEntity =
    ShoppingItemEntity(
        id = id,
        shoppingListId = shoppingListId,
        name = name,
        quantity = quantity,
        unit = unit?.name,
        isPurchased = isPurchased,
        position = position,
    )

private fun String?.toMeasurementUnit(): MeasurementUnit? =
    this?.let { storedValue ->
        MeasurementUnit.entries.firstOrNull { unit ->
            unit.name == storedValue
        }
    }
