package ru.practicum.shoppinglist.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.shoppinglist.data.local.database.ShoppingListDatabase
import ru.practicum.shoppinglist.data.local.entity.ShoppingItemEntity
import ru.practicum.shoppinglist.data.local.entity.ShoppingListEntity
import ru.practicum.shoppinglist.data.mapper.toDomain
import ru.practicum.shoppinglist.data.repository.ShoppingItemRepositoryImpl
import ru.practicum.shoppinglist.data.repository.ShoppingListRepositoryImpl

@RunWith(AndroidJUnit4::class)
class ShoppingDataRoomTest {
    private lateinit var database: ShoppingListDatabase
    private lateinit var shoppingListRepository: ShoppingListRepositoryImpl
    private lateinit var shoppingItemRepository: ShoppingItemRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ShoppingListDatabase::class.java,
        ).allowMainThreadQueries().build()
        shoppingListRepository = ShoppingListRepositoryImpl(
            database = database,
            shoppingListDao = database.shoppingListDao(),
            shoppingItemDao = database.shoppingItemDao(),
        )
        shoppingItemRepository = ShoppingItemRepositoryImpl(
            database = database,
            shoppingItemDao = database.shoppingItemDao(),
            productNameDao = database.productNameDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun duplicateShoppingListCopiesEveryItemFieldAndStatus() = runBlocking {
        val sourceListId = insertList("Поход", "camping")
        val sourceItems = listOf(
            item(sourceListId, "Вода", 2.5, "LITER", true, 0),
            item(sourceListId, "Хлеб", null, null, false, 1),
        )
        sourceItems.forEach { database.shoppingItemDao().insert(it) }

        val copiedListId = shoppingListRepository.duplicateShoppingList(sourceListId)
        assertNotNull(copiedListId)
        assertNotEquals(sourceListId, copiedListId)

        val copiedList = database.shoppingListDao().getById(requireNotNull(copiedListId))
        assertEquals("Поход", copiedList?.name)
        assertEquals("camping", copiedList?.iconKey)

        val copiedItems = database.shoppingItemDao().getByListId(copiedListId)
        assertEquals(sourceItems.map(::itemSnapshot), copiedItems.map(::itemSnapshot))
    }

    @Test
    fun duplicateShoppingListRollsBackWhenAnItemCannotBeCopied() = runBlocking {
        val sourceListId = insertList("Дом", "home")
        database.shoppingItemDao().insert(item(sourceListId, "Лампа", 1.0, "PIECE", false, 0))
        val listsBefore = database.shoppingListDao().observeAll().first().size
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER reject_copied_items
            BEFORE INSERT ON shopping_items
            WHEN NEW.list_id <> $sourceListId
            BEGIN
                SELECT RAISE(ABORT, 'copy rejected');
            END
            """.trimIndent(),
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { shoppingListRepository.duplicateShoppingList(sourceListId) }
        }

        assertEquals(listsBefore, database.shoppingListDao().observeAll().first().size)
    }

    @Test
    fun productSuggestionsAreCaseInsensitiveAndTreatWildcardsLiterally() = runBlocking {
        val listId = insertList("Продукты", "food")
        shoppingItemRepository.addShoppingItem(listId, "Молоко", null, null)
        shoppingItemRepository.addShoppingItem(listId, "100% сок", null, null)
        shoppingItemRepository.addShoppingItem(listId, "1000 островов", null, null)

        assertEquals(
            listOf("Молоко"),
            shoppingItemRepository.findProductSuggestions("МО", 10),
        )
        assertEquals(
            listOf("100% сок"),
            shoppingItemRepository.findProductSuggestions("100%", 10),
        )
    }

    @Test
    fun updatePositionsChangesOnlyOrderAndRejectsPartialLists() = runBlocking {
        val listId = insertList("Порядок", "default")
        val firstId = database.shoppingItemDao().insert(
            item(listId, "Первый", 1.0, "PIECE", true, 0),
        )
        val secondId = database.shoppingItemDao().insert(
            item(listId, "Второй", 2.0, "KILOGRAM", false, 1),
        )
        val initialItems = database.shoppingItemDao().getByListId(listId)

        shoppingItemRepository.updatePositions(
            initialItems.reversed().map { entity ->
                entity.toDomain().copy(name = "Устаревшее имя", isPurchased = !entity.isPurchased)
            },
        )

        val reordered = database.shoppingItemDao().getByListId(listId)
        assertEquals(listOf(secondId, firstId), reordered.map(ShoppingItemEntity::id))
        assertEquals(listOf(0, 1), reordered.map(ShoppingItemEntity::position))
        assertEquals(listOf("Второй", "Первый"), reordered.map(ShoppingItemEntity::name))
        assertEquals(listOf(false, true), reordered.map(ShoppingItemEntity::isPurchased))

        assertThrows(IllegalStateException::class.java) {
            runBlocking { database.shoppingItemDao().replacePositions(listId, listOf(firstId)) }
        }
        assertEquals(
            listOf(secondId, firstId),
            database.shoppingItemDao().getByListId(listId).map(ShoppingItemEntity::id),
        )
    }

    private suspend fun insertList(name: String, iconKey: String): Long =
        database.shoppingListDao().insert(
            ShoppingListEntity(
                name = name,
                iconKey = iconKey,
                createdAt = 1L,
            ),
        )

    private fun item(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: String?,
        isPurchased: Boolean,
        position: Int,
    ) = ShoppingItemEntity(
        shoppingListId = listId,
        name = name,
        quantity = quantity,
        unit = unit,
        isPurchased = isPurchased,
        position = position,
    )

    private fun itemSnapshot(item: ShoppingItemEntity) = listOf(
        item.name,
        item.quantity,
        item.unit,
        item.isPurchased,
        item.position,
    )
}
