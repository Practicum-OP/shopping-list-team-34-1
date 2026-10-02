package ru.practicum.shoppinglist.presentation.lists

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildFriendly
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DownhillSkiing
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.ui.graphics.vector.ImageVector
import ru.practicum.shoppinglist.R

internal data class ShoppingListIconOption(
    val key: String,
    val imageVector: ImageVector,
    @param:StringRes val labelRes: Int,
)

/** Stable persisted keys. Unknown and legacy values always resolve to [default]. */
internal object ShoppingListIconRegistry {
    const val DEFAULT_KEY = "list_alt"

    val options: List<ShoppingListIconOption> = listOf(
        ShoppingListIconOption("shopping_cart", Icons.Rounded.ShoppingCart, R.string.list_icon_cart),
        ShoppingListIconOption("shopping_bag", Icons.Rounded.ShoppingBag, R.string.list_icon_bag),
        ShoppingListIconOption("local_florist", Icons.Rounded.LocalFlorist, R.string.list_icon_florist),
        ShoppingListIconOption("handyman", Icons.Rounded.Handyman, R.string.list_icon_handyman),
        ShoppingListIconOption("celebration", Icons.Rounded.Celebration, R.string.list_icon_party),
        ShoppingListIconOption("cake", Icons.Rounded.Cake, R.string.list_icon_cake),
        ShoppingListIconOption("redeem", Icons.Rounded.Redeem, R.string.list_icon_redeem),
        ShoppingListIconOption("liquor", Icons.Rounded.Liquor, R.string.list_icon_liquor),
        ShoppingListIconOption("pets", Icons.Rounded.Pets, R.string.list_icon_pets),
        ShoppingListIconOption(
            "table_restaurant",
            Icons.Rounded.TableRestaurant,
            R.string.list_icon_table_restaurant,
        ),
        ShoppingListIconOption(
            "medical_services",
            Icons.Rounded.MedicalServices,
            R.string.list_icon_medical_services,
        ),
        ShoppingListIconOption("school", Icons.Rounded.School, R.string.list_icon_school),
        ShoppingListIconOption(
            "downhill_skiing",
            Icons.Rounded.DownhillSkiing,
            R.string.list_icon_downhill_skiing,
        ),
        ShoppingListIconOption("home", Icons.Rounded.Home, R.string.list_icon_home),
        ShoppingListIconOption(
            "beach_access",
            Icons.Rounded.BeachAccess,
            R.string.list_icon_beach_access,
        ),
        ShoppingListIconOption(
            "emoji_emotions",
            Icons.Rounded.EmojiEmotions,
            R.string.list_icon_emoji_emotions,
        ),
        ShoppingListIconOption(
            "sports_esports",
            Icons.Rounded.SportsEsports,
            R.string.list_icon_sports_esports,
        ),
        ShoppingListIconOption("palette", Icons.Rounded.Palette, R.string.list_icon_palette),
        ShoppingListIconOption("checkroom", Icons.Rounded.Checkroom, R.string.list_icon_checkroom),
        ShoppingListIconOption("bed", Icons.Rounded.Bed, R.string.list_icon_bed),
        ShoppingListIconOption("luggage", Icons.Rounded.Luggage, R.string.list_icon_luggage),
        ShoppingListIconOption("medication", Icons.Rounded.Medication, R.string.list_icon_medication),
        ShoppingListIconOption("food", Icons.Rounded.Restaurant, R.string.list_icon_food),
        ShoppingListIconOption(
            "photo_camera",
            Icons.Rounded.PhotoCamera,
            R.string.list_icon_photo_camera,
        ),
        ShoppingListIconOption(
            "menu_book",
            Icons.AutoMirrored.Rounded.MenuBook,
            R.string.list_icon_menu_book,
        ),
        ShoppingListIconOption(
            "self_improvement",
            Icons.Rounded.SelfImprovement,
            R.string.list_icon_self_improvement,
        ),
        ShoppingListIconOption("extension", Icons.Rounded.Extension, R.string.list_icon_extension),
        ShoppingListIconOption("devices", Icons.Rounded.Devices, R.string.list_icon_devices),
        ShoppingListIconOption(
            "child_friendly",
            Icons.Rounded.ChildFriendly,
            R.string.list_icon_child_friendly,
        ),
        ShoppingListIconOption(
            DEFAULT_KEY,
            Icons.AutoMirrored.Rounded.ListAlt,
            R.string.list_icon_list_alt,
        ),
    )

    val default: ShoppingListIconOption = options.last()

    private val optionsByKey = options.associateBy(ShoppingListIconOption::key)
    private val legacyOptionsByKey = mapOf(
        "favorite" to default,
    )

    fun resolve(iconKey: String?): ShoppingListIconOption =
        optionsByKey[iconKey] ?: legacyOptionsByKey[iconKey] ?: default
}
