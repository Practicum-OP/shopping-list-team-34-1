package ru.practicum.shoppinglist.data.mapper

import ru.practicum.shoppinglist.data.local.entity.ShoppingListEntity
import ru.practicum.shoppinglist.domain.api.model.ShoppingList

internal fun ShoppingListEntity.toDomain(): ShoppingList =
    ShoppingList(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAt = createdAt,
    )

internal fun ShoppingList.toEntity(): ShoppingListEntity =
    ShoppingListEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAt = createdAt,
    )
