package ru.practicum.shoppinglist.presentation.editor

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val editorModule = module {
    single<ShoppingItemSortStorage> {
        SharedPreferencesShoppingItemSortStorage(
            preferences = androidContext().getSharedPreferences(
                SharedPreferencesShoppingItemSortStorage.PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            ),
        )
    }

    viewModel { parameters ->
        ShoppingListEditorViewModel(
            listId = parameters.get(),
            shoppingListInteractor = get(),
            shoppingItemInteractor = get(),
            sortStorage = get(),
        )
    }
}
