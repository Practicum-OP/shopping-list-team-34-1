package ru.practicum.shoppinglist.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Locale

internal object ShoppingListMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS product_names (
                    normalized_name TEXT NOT NULL PRIMARY KEY,
                    display_name TEXT NOT NULL,
                    last_used_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            seedProductNameHistory(db)

            db.execSQL("DROP INDEX IF EXISTS index_shopping_items_list_id")
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_shopping_items_list_id_position
                ON shopping_items (list_id, position)
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_product_names_last_used_at
                ON product_names (last_used_at)
                """.trimIndent(),
            )
        }
    }

    private fun seedProductNameHistory(database: SupportSQLiteDatabase) {
        database.query("SELECT name FROM shopping_items ORDER BY id ASC").use { cursor ->
            while (cursor.moveToNext()) {
                val displayName = cursor.getString(0).trim()
                if (displayName.isNotEmpty()) {
                    database.execSQL(
                        """
                        INSERT OR IGNORE INTO product_names(
                            normalized_name,
                            display_name,
                            last_used_at
                        ) VALUES (?, ?, 0)
                        """.trimIndent(),
                        arrayOf(displayName.lowercase(Locale.ROOT), displayName),
                    )
                }
            }
        }
    }
}
