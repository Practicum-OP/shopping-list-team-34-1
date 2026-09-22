package ru.practicum.shoppinglist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity

@Dao
internal interface ShoppingItemDao {

    @Query(
        "SELECT * FROM shopping_items " +
                "WHERE list_id = :listId ORDER BY position ASC",
    )
    fun observeByListId(listId: Long): Flow<List<ShoppingItemEntity>>


    @Query(
        "SELECT * FROM shopping_items " +
                "WHERE list_id = :listId ORDER BY position ASC",
    )
    suspend fun getByListId(listId: Long): List<ShoppingItemEntity>

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 " +
                "FROM shopping_items WHERE list_id = :listId",
    )
    suspend fun getNextPosition(listId: Long): Int

    @Insert
    suspend fun insertAll(
        shoppingItems: List<ShoppingItemEntity>,
    )

    @Insert
    suspend fun insert(shoppingItem: ShoppingItemEntity): Long

    @Update
    suspend fun update(shoppingItem: ShoppingItemEntity)

    @Update
    suspend fun updateAll(shoppingItems: List<ShoppingItemEntity>)

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
