package ru.practicum.shoppinglist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица подсказок

@Entity(tableName = "product_names")
data class ProductNameEntity(
    @PrimaryKey
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long,
)
