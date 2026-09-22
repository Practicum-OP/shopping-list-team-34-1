package ru.practicum.shoppinglist.data.repository

import androidx.room.withTransaction
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.shoppinglist.data.local.dao.ProductNameDao
import ru.practicum.shoppinglist.data.local.dao.ShoppingItemDao
import ru.practicum.shoppinglist.data.local.database.ShoppingListDatabase
import ru.practicum.shoppinglist.data.local.entity.ProductNameEntity
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity
import ru.practicum.shoppinglist.data.mapper.toDomain
import ru.practicum.shoppinglist.data.mapper.toEntity
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.repository.ShoppingItemRepository

internal class ShoppingItemRepositoryImpl(
    private val database: ShoppingListDatabase,
    private val shoppingItemDao: ShoppingItemDao,
    private val productNameDao: ProductNameDao,
) : ShoppingItemRepository {

    override fun observeShoppingItems(
        listId: Long,
    ): Flow<List<ShoppingItem>> =
        shoppingItemDao.observeByListId(listId).map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }

    override suspend fun getShoppingItems(
        listId: Long,
    ): List<ShoppingItem> =
        shoppingItemDao.getByListId(listId).map { entity ->
            entity.toDomain()
        }

    override suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long =
        database.withTransaction {
            val displayName = name.trim()
            val position = shoppingItemDao.getNextPosition(listId)

            val itemId = shoppingItemDao.insert(
                ShoppingItemEntity(
                    shoppingListId = listId,
                    name = displayName,
                    quantity = quantity,
                    unit = unit?.name,
                    isPurchased = false,
                    position = position,
                ),
            )

            saveProductName(displayName)

            itemId
        }

    override suspend fun updateShoppingItem(
        shoppingItem: ShoppingItem,
    ) {
        database.withTransaction {
            val displayName = shoppingItem.name.trim()

            shoppingItemDao.update(
                shoppingItem.copy(name = displayName).toEntity(),
            )

            saveProductName(displayName)
        }
    }

    override suspend fun setPurchased(
        itemId: Long,
        isPurchased: Boolean,
    ) {
        shoppingItemDao.setPurchased(
            itemId = itemId,
            isPurchased = isPurchased,
        )
    }

    override suspend fun deleteShoppingItem(itemId: Long) {
        shoppingItemDao.deleteById(itemId)
    }

    override suspend fun deletePurchasedItems(listId: Long) {
        shoppingItemDao.deletePurchased(listId)
    }

    override suspend fun updatePositions(
        shoppingItems: List<ShoppingItem>,
    ) {
        val entities = shoppingItems.map { shoppingItem ->
            shoppingItem.toEntity()
        }

        shoppingItemDao.updateAll(entities)
    }

    override suspend fun findProductSuggestions(
        query: String,
        limit: Int,
    ): List<String> {
        val normalizedQuery = query
            .trim()
            .lowercase(Locale.ROOT)

        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        return productNameDao.findSuggestions(
            normalizedQuery = normalizedQuery,
            limit = limit,
        )
    }

    private suspend fun saveProductName(name: String) {
        if (name.isBlank()) {
            return
        }

        productNameDao.upsert(
            ProductNameEntity(
                normalizedName = name.lowercase(Locale.ROOT),
                displayName = name,
                lastUsedAt = System.currentTimeMillis(),
            ),
        )
    }
}
