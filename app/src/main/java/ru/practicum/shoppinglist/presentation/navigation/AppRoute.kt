package ru.practicum.shoppinglist.presentation.navigation

internal object AppRoute {
    const val AUTH_GRAPH = "auth_graph"
    const val AUTH_CHECK = "auth_check"
    const val LOGIN = "login"
    const val REGISTRATION = "registration"
    const val PASSWORD_RECOVERY = "password_recovery"

    const val SHOPPING_LISTS = "shopping_lists"
    const val LIST_EDITOR = "list_editor/{listId}/{listName}"

    fun createListEditorRoute(
        listId: Long,
        listName: String,
    ): String {
        val encodedName = java.net.URLEncoder.encode(
            listName,
            "UTF-8",
        )
        return "list_editor/$listId/$encodedName"
    }
}
