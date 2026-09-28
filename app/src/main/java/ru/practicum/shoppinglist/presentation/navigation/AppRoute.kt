package ru.practicum.shoppinglist.presentation.navigation

internal object AppRoute {
    const val SHOPPING_LISTS = "shopping_lists"
    const val LIST_EDITOR = "list_editor/{listId}/{listName}"

    fun createListEditorRoute(listId: Long, listName: String): String {
        val encodedName = java.net.URLEncoder.encode(listName, "UTF-8")
        return "list_editor/$listId/$encodedName"
    }
}