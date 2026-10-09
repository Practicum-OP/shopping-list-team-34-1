package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R

internal const val ICON_PICKER_GRID_TEST_TAG = "shopping_list_icon_grid"
internal const val ICON_PICKER_OPTION_TEST_TAG_PREFIX = "shopping_list_icon_option_"

@Composable
internal fun ShoppingListIconPickerField(
    selectedIconKey: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val selectedOption = ShoppingListIconRegistry.resolve(selectedIconKey)
    val selectedLabel = stringResource(selectedOption.labelRes)
    val selectedIconDescription = stringResource(R.string.lists_selected_icon, selectedLabel)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = selectedIconDescription
            },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        ShoppingListIconPickerFieldContent(
            selectedOption = selectedOption,
            selectedIconDescription = selectedIconDescription,
            enabled = enabled,
        )
    }
}

@Composable
private fun ShoppingListIconPickerFieldContent(
    selectedOption: ShoppingListIconOption,
    selectedIconDescription: String,
    enabled: Boolean,
) {
    val contentAlpha = if (enabled) 1f else DISABLED_ALPHA
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = selectedOption.imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = selectedIconDescription,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
        )
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShoppingListIconPickerSheet(
    selectedIconKey: String,
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.lists_choose_icon),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(20.dp))
            ShoppingListIconGrid(
                selectedIconKey = selectedIconKey,
                onIconSelected = onIconSelected,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun ShoppingListIconGrid(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
) {
    val resolvedSelectedKey = ShoppingListIconRegistry.resolve(selectedIconKey).key
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .height(328.dp)
            .testTag(ICON_PICKER_GRID_TEST_TAG),
        columns = GridCells.Fixed(ICON_GRID_COLUMNS),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(
            items = ShoppingListIconRegistry.options,
            key = ShoppingListIconOption::key,
        ) { option ->
            ShoppingListIconPickerOption(
                option = option,
                isSelected = option.key == resolvedSelectedKey,
                onClick = { onIconSelected(option.key) },
            )
        }
    }
}

@Composable
private fun ShoppingListIconPickerOption(
    option: ShoppingListIconOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(option.labelRes)
    Surface(
        modifier = Modifier
            .size(48.dp)
            .testTag(ICON_PICKER_OPTION_TEST_TAG_PREFIX + option.key)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics {
                contentDescription = label
            },
        shape = CircleShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Icon(
            modifier = Modifier.padding(12.dp),
            imageVector = option.imageVector,
            contentDescription = null,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

private const val DISABLED_ALPHA = 0.38f
private const val ICON_GRID_COLUMNS = 5
