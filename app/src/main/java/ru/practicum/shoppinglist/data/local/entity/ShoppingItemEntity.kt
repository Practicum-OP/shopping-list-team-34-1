package ru.practicum.shoppinglist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["list_id", "position"]),
    ],
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "list_id")
    val shoppingListId: Long,
    val name: String,
    val quantity: Double?,
    val unit: String?,
    @ColumnInfo(name = "is_purchased")
    val isPurchased: Boolean,
    val position: Int,
)
