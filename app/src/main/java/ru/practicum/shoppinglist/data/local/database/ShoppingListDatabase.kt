package ru.practicum.shoppinglist.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.practicum.shoppinglist.data.local.dao.ProductNameDao
import ru.practicum.shoppinglist.data.local.dao.ShoppingItemDao
import ru.practicum.shoppinglist.data.local.dao.ShoppingListDao
import ru.practicum.shoppinglist.data.local.entity.ProductNameEntity
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity
import ru.practicum.shoppinglist.data.local.entity.ShoppingListEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        ProductNameEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
internal abstract class ShoppingListDatabase : RoomDatabase() {

    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun productNameDao(): ProductNameDao

    companion object {
        const val DATABASE_NAME = "shopping_list.db"
    }
}
