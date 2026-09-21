package ru.practicum.shoppinglist.presentation.navigation

internal object AppRoute {
    const val SHOPPING_LISTS = "shopping_lists"
    const val LIST_EDITOR = "list_editor"
    const val LIST_ID_ARGUMENT = "listId"
    const val LIST_EDITOR_ROUTE =
        "$LIST_EDITOR?$LIST_ID_ARGUMENT={$LIST_ID_ARGUMENT}"

    fun listEditor(listId: Long? = null): String =
        listId?.let { "$LIST_EDITOR?$LIST_ID_ARGUMENT=$it" }
            ?: LIST_EDITOR
}
