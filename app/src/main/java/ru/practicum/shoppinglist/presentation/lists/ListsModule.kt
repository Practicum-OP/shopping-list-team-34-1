package ru.practicum.shoppinglist.presentation.lists

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val listsModule = module {
    viewModelOf(::ShoppingListsViewModel)
}
