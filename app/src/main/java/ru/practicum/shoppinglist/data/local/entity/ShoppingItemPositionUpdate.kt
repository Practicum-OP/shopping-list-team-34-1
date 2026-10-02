package ru.practicum.shoppinglist.data.local.entity

import androidx.room.ColumnInfo

/** Partial Room entity used to reorder items without overwriting their editable fields. */
internal data class ShoppingItemPositionUpdate(
    @ColumnInfo(name = "id")
    val id: Long,
    val position: Int,
)
