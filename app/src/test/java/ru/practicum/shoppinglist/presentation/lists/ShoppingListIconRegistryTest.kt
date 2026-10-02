package ru.practicum.shoppinglist.presentation.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ShoppingListIconRegistryTest {

    private val expectedKeys = listOf(
        "shopping_cart",
        "shopping_bag",
        "local_florist",
        "handyman",
        "celebration",
        "cake",
        "redeem",
        "liquor",
        "pets",
        "table_restaurant",
        "medical_services",
        "school",
        "downhill_skiing",
        "home",
        "beach_access",
        "emoji_emotions",
        "sports_esports",
        "palette",
        "checkroom",
        "bed",
        "luggage",
        "medication",
        "food",
        "photo_camera",
        "menu_book",
        "self_improvement",
        "extension",
        "devices",
        "child_friendly",
        "list_alt",
    )

    @Test
    fun `registry contains exactly thirty icons in the specified grid order`() {
        assertEquals(expectedKeys, ShoppingListIconRegistry.options.map { it.key })
        assertEquals(30, ShoppingListIconRegistry.options.size)
    }

    @Test
    fun `unknown key resolves to default icon`() {
        assertSame(
            ShoppingListIconRegistry.default,
            ShoppingListIconRegistry.resolve("removed_icon_key"),
        )
    }

    @Test
    fun `icon keys are unique and stable default is exposed`() {
        val keys = ShoppingListIconRegistry.options.map { it.key }

        assertEquals(keys.distinct(), keys)
        assertEquals("list_alt", ShoppingListIconRegistry.DEFAULT_KEY)
        assertEquals(ShoppingListIconRegistry.DEFAULT_KEY, ShoppingListIconRegistry.default.key)
        assertSame(ShoppingListIconRegistry.options.last(), ShoppingListIconRegistry.default)
    }

    @Test
    fun `legacy favorite key resolves safely to default icon`() {
        assertSame(
            ShoppingListIconRegistry.default,
            ShoppingListIconRegistry.resolve("favorite"),
        )
    }

    @Test
    fun `previously persisted current keys keep their meaning`() {
        listOf("food", "home", "pets", "celebration", "shopping_cart").forEach { key ->
            assertEquals(key, ShoppingListIconRegistry.resolve(key).key)
        }
    }
}
