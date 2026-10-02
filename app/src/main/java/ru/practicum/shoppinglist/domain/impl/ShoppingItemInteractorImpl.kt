package ru.practicum.shoppinglist.domain.impl

import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.domain.api.repository.ShoppingItemRepository

private const val MAX_SUGGESTION_LIMIT = 20

internal class ShoppingItemInteractorImpl(
    private val repository: ShoppingItemRepository,
) : ShoppingItemInteractor {

    override fun observeShoppingItems(
        listId: Long,
        sort: ShoppingItemSort,
    ): Flow<List<ShoppingItem>> =
        repository.observeShoppingItems(listId).map { shoppingItems ->
            when (sort) {
                ShoppingItemSort.MANUAL -> shoppingItems
                ShoppingItemSort.ALPHABETICAL ->
                    shoppingItems.sortedAlphabetically()
            }
        }

    override suspend fun addShoppingItem(
        listId: Long,
        name: String,
        quantity: Double?,
        unit: MeasurementUnit?,
    ): Long? {
        val preparedName = name.trim()

        if (
            listId <= 0 ||
            preparedName.isBlank() ||
            !isQuantityValid(quantity)
        ) {
            return null
        }

        return repository.addShoppingItem(
            listId = listId,
            name = preparedName,
            quantity = quantity,
            unit = unit.takeIf { quantity != null },
        )
    }

    override suspend fun updateShoppingItem(
        shoppingItem: ShoppingItem,
    ): Boolean {
        val preparedName = shoppingItem.name.trim()

        if (
            shoppingItem.id <= 0 ||
            preparedName.isBlank() ||
            !isQuantityValid(shoppingItem.quantity)
        ) {
            return false
        }

        repository.updateShoppingItem(
            shoppingItem.copy(
                name = preparedName,
                unit = shoppingItem.unit.takeIf {
                    shoppingItem.quantity != null
                },
            ),
        )

        return true
    }

    override suspend fun setPurchased(
        itemId: Long,
        isPurchased: Boolean,
    ) {
        if (itemId > 0) {
            repository.setPurchased(
                itemId = itemId,
                isPurchased = isPurchased,
            )
        }
    }

    override suspend fun deleteShoppingItem(itemId: Long) {
        if (itemId > 0) {
            repository.deleteShoppingItem(itemId)
        }
    }

    override suspend fun deletePurchasedItems(listId: Long) {
        if (listId > 0) {
            repository.deletePurchasedItems(listId)
        }
    }

    override suspend fun updatePositions(
        shoppingItems: List<ShoppingItem>,
    ) {
        if (!shoppingItems.haveValidOrderIdentity()) return

        val itemsWithUpdatedPositions =
            shoppingItems.mapIndexed { index, shoppingItem ->
                shoppingItem.copy(position = index)
            }

        repository.updatePositions(itemsWithUpdatedPositions)
    }

    private fun List<ShoppingItem>.haveValidOrderIdentity(): Boolean {
        if (isEmpty()) return false

        val listId = first().shoppingListId
        return listId > 0 &&
                all { item -> item.id > 0 && item.shoppingListId == listId } &&
                map(ShoppingItem::id).distinct().size == size
    }

    override suspend fun findProductSuggestions(
        query: String,
        limit: Int,
    ): List<String> {
        val preparedQuery = query.trim()

        if (preparedQuery.isBlank()) {
            return emptyList()
        }

        return repository.findProductSuggestions(
            query = preparedQuery,
            limit = limit.coerceIn(
                minimumValue = 1,
                maximumValue = MAX_SUGGESTION_LIMIT,
            ),
        )
    }

    private fun isQuantityValid(quantity: Double?): Boolean =
        quantity == null || (
                quantity.isFinite() &&
                        quantity > 0
                )

    private fun List<ShoppingItem>.sortedAlphabetically():
            List<ShoppingItem> {
        val collator = Collator.getInstance(
            Locale.forLanguageTag("ru"),
        ).apply {
            strength = Collator.PRIMARY
        }

        return sortedWith { firstItem, secondItem ->
            val comparison = collator.compare(
                firstItem.name,
                secondItem.name,
            )

            if (comparison == 0) {
                firstItem.position.compareTo(secondItem.position)
            } else {
                comparison
            }
        }
    }
}
