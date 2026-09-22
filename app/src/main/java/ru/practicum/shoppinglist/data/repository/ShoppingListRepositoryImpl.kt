package ru.practicum.shoppinglist.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.shoppinglist.data.local.dao.ShoppingItemDao
import ru.practicum.shoppinglist.data.local.dao.ShoppingListDao
import ru.practicum.shoppinglist.data.local.database.ShoppingListDatabase
import ru.practicum.shoppinglist.data.local.entity.ShoppingListEntity
import ru.practicum.shoppinglist.data.mapper.toDomain
import ru.practicum.shoppinglist.data.mapper.toEntity
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.domain.api.repository.ShoppingListRepository

internal class ShoppingListRepositoryImpl(
    private val database: ShoppingListDatabase,
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao,
) : ShoppingListRepository {

    override fun observeShoppingLists(): Flow<List<ShoppingList>> =
        shoppingListDao.observeAll().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }

    override suspend fun getShoppingList(
        listId: Long,
    ): ShoppingList? =
        shoppingListDao.getById(listId)?.toDomain()

    override suspend fun createShoppingList(
        name: String,
        iconKey: String,
    ): Long =
        shoppingListDao.insert(
            ShoppingListEntity(
                name = name,
                iconKey = iconKey,
                createdAt = System.currentTimeMillis(),
            ),
        )

    override suspend fun updateShoppingList(
        shoppingList: ShoppingList,
    ) {
        shoppingListDao.update(shoppingList.toEntity())
    }

    override suspend fun deleteShoppingList(listId: Long) {
        shoppingListDao.deleteById(listId)
    }

    override suspend fun duplicateShoppingList(
        listId: Long,
    ): Long? =
        database.withTransaction {
            val sourceList = shoppingListDao.getById(listId)
                ?: return@withTransaction null

            val sourceItems = shoppingItemDao.getByListId(listId)

            val newListId = shoppingListDao.insert(
                sourceList.copy(
                    id = 0,
                    createdAt = System.currentTimeMillis(),
                ),
            )

            val copiedItems = sourceItems.map { sourceItem ->
                sourceItem.copy(
                    id = 0,
                    shoppingListId = newListId,
                    isPurchased = false,
                )
            }

            if (copiedItems.isNotEmpty()) {
                shoppingItemDao.insertAll(copiedItems)
            }

            newListId
        }
}

