package ru.practicum.shoppinglist.domain.api.model

data class ShoppingItem(
    val id: Long,
    val shoppingListId: Long,
    val name: String,
    val quantity: Double?,
    val unit: MeasurementUnit?,
    val isPurchased: Boolean,
    val position: Int,
)
