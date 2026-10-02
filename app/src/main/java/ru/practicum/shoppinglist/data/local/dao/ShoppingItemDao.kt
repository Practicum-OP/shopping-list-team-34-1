package ru.practicum.shoppinglist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemPositionUpdate

@Dao
internal interface ShoppingItemDao {

    @Query(
        "SELECT * FROM shopping_items " +
        "WHERE list_id = :listId ORDER BY position ASC, id ASC",
    )
    fun observeByListId(listId: Long): Flow<List<ShoppingItemEntity>>


    @Query(
        "SELECT * FROM shopping_items " +
        "WHERE list_id = :listId ORDER BY position ASC, id ASC",
    )
    suspend fun getByListId(listId: Long): List<ShoppingItemEntity>

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 " +
                "FROM shopping_items WHERE list_id = :listId",
    )
    suspend fun getNextPosition(listId: Long): Int

    @Query("SELECT id FROM shopping_items WHERE list_id = :listId")
    suspend fun getIdsByListId(listId: Long): List<Long>

    @Insert
    suspend fun insert(shoppingItem: ShoppingItemEntity): Long

    @Update
    suspend fun update(shoppingItem: ShoppingItemEntity)

    @Update(entity = ShoppingItemEntity::class)
    suspend fun updatePositions(
        positions: List<ShoppingItemPositionUpdate>,
    ): Int

    @Transaction
    suspend fun replacePositions(
        listId: Long,
        orderedItemIds: List<Long>,
    ) {
        require(orderedItemIds.distinct().size == orderedItemIds.size) {
            "Item order must not contain duplicate ids"
        }

        val storedItemIds = getIdsByListId(listId)
        check(
            storedItemIds.size == orderedItemIds.size &&
                    storedItemIds.toSet() == orderedItemIds.toSet(),
        ) { "Item order must contain every item from the list exactly once" }

        val positions = orderedItemIds.mapIndexed { position, itemId ->
            ShoppingItemPositionUpdate(id = itemId, position = position)
        }
        check(updatePositions(positions) == positions.size) {
            "Not all item positions were updated"
        }
    }

    @Query(
        "UPDATE shopping_items SET is_purchased = :isPurchased " +
                "WHERE id = :itemId",
    )
    suspend fun setPurchased(
        itemId: Long,
        isPurchased: Boolean,
    )

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    @Query(
        "DELETE FROM shopping_items " +
                "WHERE list_id = :listId AND is_purchased = 1",
    )
    suspend fun deletePurchased(listId: Long)
}
