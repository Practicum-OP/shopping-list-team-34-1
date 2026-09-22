package ru.practicum.shoppinglist.domain.api.model

data class ShoppingList(
    val id: Long,
    val name: String,
    val iconKey: String,
    val createdAt: Long,
)
