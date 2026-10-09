package ru.practicum.shoppinglist.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.shoppinglist.data.local.database.ShoppingListDatabase
import ru.practicum.shoppinglist.data.local.database.ShoppingListMigrations

@RunWith(AndroidJUnit4::class)
class ShoppingListMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation,
        ShoppingListDatabase::class.java,
    )

    @Test
    fun migration1To2PreservesDataAndSeedsProductHistory() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO shopping_lists(id, name, icon_key, created_at) " +
                        "VALUES (1, 'Продукты', 'food', 10)",
            )
            execSQL(
                "INSERT INTO shopping_items(" +
                        "id, list_id, name, quantity, unit, is_purchased, position" +
                        ") VALUES (1, 1, 'Хлеб', NULL, NULL, 1, 0)",
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            ShoppingListMigrations.MIGRATION_1_2,
        ).use { database ->
            database.query(
                "SELECT display_name FROM product_names WHERE normalized_name = 'хлеб'",
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("Хлеб", cursor.getString(0))
            }
            database.query("SELECT is_purchased, position FROM shopping_items WHERE id = 1")
                .use { cursor ->
                    assertEquals(true, cursor.moveToFirst())
                    assertEquals(1, cursor.getInt(0))
                    assertEquals(0, cursor.getInt(1))
                }
        }
    }

    private companion object {
        const val TEST_DATABASE = "shopping-list-migration-test"
    }
}
