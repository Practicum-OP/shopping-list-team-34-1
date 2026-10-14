package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.ui.theme.ShoppingListTheme

class ShoppingListsSwipeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun endToStartSwipeRevealsActionsAndDispatchesDelete() {
        val list = ShoppingList(
            id = LIST_ID,
            name = LIST_NAME,
            iconKey = ShoppingListIconRegistry.DEFAULT_KEY,
            createdAt = 1L,
        )
        val actions = mutableListOf<ShoppingListsAction>()

        composeRule.setContent {
            ShoppingListTheme {
                ShoppingListsContent(
                    state = ShoppingListsUiState(
                        lists = listOf(list),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = actions::add,
                    onItemClick = { _, _ -> },
                )
            }
        }

        val swipeContainer = composeRule.onNodeWithTag(
            SHOPPING_LIST_SWIPE_TEST_TAG_PREFIX + LIST_ID,
        )
        swipeContainer.performTouchInput {
            swipeLeft(durationMillis = SWIPE_DURATION_MILLIS)
        }
        composeRule.waitForIdle()

        swipeContainer.performTouchInput {
            click(centerRight - Offset(DELETE_ACTION_INSET_PX, 0f))
        }
        composeRule.runOnIdle {
            assertEquals(
                ShoppingListsAction.DeleteListClicked(list),
                actions.single(),
            )
        }
    }

    private companion object {
        const val LIST_ID = 41L
        const val LIST_NAME = "На неделю"
        const val SWIPE_DURATION_MILLIS = 600L
        const val DELETE_ACTION_INSET_PX = 36f
    }
}
