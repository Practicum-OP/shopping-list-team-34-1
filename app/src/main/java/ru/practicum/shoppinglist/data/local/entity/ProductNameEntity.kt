package ru.practicum.shoppinglist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_names",
    indices = [Index(value = ["last_used_at"])],
)
data class ProductNameEntity(
    @PrimaryKey
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long,
)
