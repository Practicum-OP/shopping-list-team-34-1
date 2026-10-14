package ru.practicum.shoppinglist.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.practicum.shoppinglist.data.local.entity.ProductNameEntity

@Dao
internal interface ProductNameDao {

    @Upsert
    suspend fun upsert(productName: ProductNameEntity)

    @Query(
        "SELECT display_name FROM product_names " +
                "WHERE substr(normalized_name, 1, length(:normalizedQuery)) = :normalizedQuery " +
                "ORDER BY last_used_at DESC, normalized_name ASC LIMIT :limit",
    )
    suspend fun findSuggestions(
        normalizedQuery: String,
        limit: Int,
    ): List<String>
}
