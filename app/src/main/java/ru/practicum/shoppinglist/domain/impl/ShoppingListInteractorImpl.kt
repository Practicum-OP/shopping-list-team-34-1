package ru.practicum.shoppinglist.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.domain.api.repository.ShoppingListRepository

internal class ShoppingListInteractorImpl(
    private val repository: ShoppingListRepository,
) : ShoppingListInteractor {

    override fun observeShoppingLists(): Flow<List<ShoppingList>> =
        repository.observeShoppingLists()

    override suspend fun getShoppingList(
        listId: Long,
    ): ShoppingList? =
        repository.getShoppingList(listId)

    override suspend fun createShoppingList(
        name: String,
        iconKey: String,
    ): Long? {
        val preparedName = name.trim()
        val preparedIconKey = iconKey.trim()

        if (preparedName.isBlank() || preparedIconKey.isBlank()) {
            return null
        }

        return repository.createShoppingList(
            name = preparedName,
            iconKey = preparedIconKey,
        )
    }

    override suspend fun updateShoppingList(
        shoppingList: ShoppingList,
    ): Boolean {
        val preparedName = shoppingList.name.trim()
        val preparedIconKey = shoppingList.iconKey.trim()

        if (preparedName.isBlank() || preparedIconKey.isBlank()) {
            return false
        }

        repository.updateShoppingList(
            shoppingList.copy(
                name = preparedName,
                iconKey = preparedIconKey,
            ),
        )

        return true
    }

    override suspend fun deleteShoppingList(listId: Long) {
        repository.deleteShoppingList(listId)
    }

    override suspend fun duplicateShoppingList(
        listId: Long,
    ): Long? =
        repository.duplicateShoppingList(listId)
}
