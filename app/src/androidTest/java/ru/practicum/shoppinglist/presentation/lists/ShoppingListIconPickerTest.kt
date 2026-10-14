package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.practicum.shoppinglist.ui.theme.ShoppingListTheme

class ShoppingListIconPickerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun iconGridShowsFigmaSetAndAppliesSelection() {
        var selectedKey = ShoppingListIconRegistry.DEFAULT_KEY

        composeRule.setContent {
            var currentKey by remember { mutableStateOf(selectedKey) }
            ShoppingListTheme {
                ShoppingListIconGrid(
                    selectedIconKey = currentKey,
                    onIconSelected = { iconKey ->
                        currentKey = iconKey
                        selectedKey = iconKey
                    },
                )
            }
        }

        ShoppingListIconRegistry.options.forEach { option ->
            composeRule.onNodeWithTag(
                ICON_PICKER_OPTION_TEST_TAG_PREFIX + option.key,
            ).assertIsDisplayed()
        }

        composeRule.onNodeWithTag(
            ICON_PICKER_OPTION_TEST_TAG_PREFIX + "home",
        ).performClick()

        composeRule.onNodeWithTag(
            ICON_PICKER_OPTION_TEST_TAG_PREFIX + "home",
        ).assertIsSelected()
        composeRule.runOnIdle {
            assertEquals("home", selectedKey)
        }
    }
}
