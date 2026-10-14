package ru.practicum.shoppinglist.presentation.editor

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val editorModule = module {
    viewModelOf(::ListEditorViewModel)
}
