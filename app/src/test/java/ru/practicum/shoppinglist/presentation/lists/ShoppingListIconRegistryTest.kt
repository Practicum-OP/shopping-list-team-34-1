package ru.practicum.shoppinglist.presentation.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ShoppingListIconRegistryTest {

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
        assertEquals(ShoppingListIconRegistry.DEFAULT_KEY, ShoppingListIconRegistry.default.key)
    }
}
