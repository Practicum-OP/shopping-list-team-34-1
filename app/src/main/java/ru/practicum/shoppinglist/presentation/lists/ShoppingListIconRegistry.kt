package ru.practicum.shoppinglist.presentation.lists

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import ru.practicum.shoppinglist.R

internal data class ShoppingListIconOption(
    val key: String,
    val imageVector: ImageVector,
    @param:StringRes val labelRes: Int,
)

/** Stable persisted keys. Unknown and legacy values always resolve to [default]. */
internal object ShoppingListIconRegistry {
    const val DEFAULT_KEY = "shopping_cart"

    val options: List<ShoppingListIconOption> = listOf(
        ShoppingListIconOption(DEFAULT_KEY, Icons.Rounded.ShoppingCart, R.string.list_icon_cart),
        ShoppingListIconOption("food", Icons.Rounded.Restaurant, R.string.list_icon_food),
        ShoppingListIconOption("home", Icons.Rounded.Home, R.string.list_icon_home),
        ShoppingListIconOption("pets", Icons.Rounded.Pets, R.string.list_icon_pets),
        ShoppingListIconOption("celebration", Icons.Rounded.Celebration, R.string.list_icon_party),
        ShoppingListIconOption("favorite", Icons.Rounded.Favorite, R.string.list_icon_favorite),
    )

    val default: ShoppingListIconOption = options.first()

    private val optionsByKey = options.associateBy(ShoppingListIconOption::key)

    fun resolve(iconKey: String?): ShoppingListIconOption = optionsByKey[iconKey] ?: default
}
