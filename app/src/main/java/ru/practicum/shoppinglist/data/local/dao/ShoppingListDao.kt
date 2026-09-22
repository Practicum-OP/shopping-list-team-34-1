package ru.practicum.shoppinglist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.data.local.entity.ShoppingListEntity

@Dao
internal interface ShoppingListDao {

    @Query("SELECT * FROM shopping_lists ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId LIMIT 1")
    suspend fun getById(listId: Long): ShoppingListEntity?

    @Insert
    suspend fun insert(shoppingList: ShoppingListEntity): Long

    @Update
    suspend fun update(shoppingList: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun deleteById(listId: Long)
}
