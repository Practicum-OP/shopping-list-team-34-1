package ru.practicum.shoppinglist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
