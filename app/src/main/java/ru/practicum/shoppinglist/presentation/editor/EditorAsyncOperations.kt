package ru.practicum.shoppinglist.presentation.editor

import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

internal class ListEditorImprovementController(
    private val state: MutableStateFlow<ListEditorUiState>,
    scope: CoroutineScope,
    interactor: ShoppingItemInteractor,
    private val onFailure: () -> Unit,
) {
    private var latestManualItems: List<ShoppingItem> = emptyList()
    private var preferredOrderIds: List<Long>? = null
    private val suggestionLoader = ProductSuggestionLoader(
        scope = scope,
        interactor = interactor,
        onLoaded = ::applySuggestions,
    )
    private val positionQueue = ItemPositionUpdateQueue(
        scope = scope,
        interactor = interactor,
        resolveItems = ::resolveItemsForSave,
        callbacks = PositionSaveCallbacks(
            onSaved = ::handlePositionSaved,
            onFailure = ::handlePositionSaveFailure,
            onIdle = ::refreshManualItems,
        ),
    )

    fun acceptManualItems(items: List<ShoppingItem>): List<ShoppingItem> {
        latestManualItems = items
        preferredOrderIds = preferredOrderIds?.takeUnless { orderIds ->
            mergeOrderIds(orderIds, items) == items.map(ShoppingItem::id)
        }
        return visibleManualItems()
    }

    fun requestSuggestions(query: String) {
        suggestionLoader.request(query)
    }

    fun cancelSuggestions() {
        suggestionLoader.cancel()
    }

    fun dismissSuggestions() {
        cancelSuggestions()
        state.update { currentState ->
            currentState.copy(
                createDialog = currentState.createDialog?.copy(
                    suggestions = emptyList(),
                ),
            )
        }
    }

    fun moveItem(
        itemId: Long,
        direction: Int,
    ) {
        val currentState = state.value
        if (
            currentState.sortType != ShoppingItemSort.MANUAL ||
            currentState.isSubmitting ||
            direction == 0
        ) {
            return
        }

        val currentItems = currentState.items.toMutableList()
        val fromIndex = currentItems.indexOfFirst { item -> item.id == itemId }
        val toIndex = fromIndex + direction
        if (fromIndex !in currentItems.indices || toIndex !in currentItems.indices) return

        currentItems.add(toIndex, currentItems.removeAt(fromIndex))
        val reorderedItems = currentItems.mapIndexed { index, item ->
            item.copy(position = index)
        }
        val orderIds = reorderedItems.map(ShoppingItem::id)
        preferredOrderIds = orderIds
        state.update { it.copy(items = reorderedItems) }
        positionQueue.enqueue(orderIds)
    }

    private fun applySuggestions(
        query: String,
        suggestions: List<String>,
    ) {
        state.update { currentState ->
            val dialog = currentState.createDialog
            if (dialog?.name == query) {
                currentState.copy(
                    createDialog = dialog.copy(suggestions = suggestions),
                )
            } else {
                currentState
            }
        }
    }

    private fun handlePositionSaved(orderIds: List<Long>) {
        if (preferredOrderIds == orderIds) {
            preferredOrderIds = orderIds.takeUnless { savedOrderIds ->
                mergeOrderIds(savedOrderIds, latestManualItems) ==
                        latestManualItems.map(ShoppingItem::id)
            }
        }
        refreshManualItems()
    }

    private fun handlePositionSaveFailure() {
        preferredOrderIds = null
        refreshManualItems()
        onFailure()
    }

    private fun refreshManualItems() {
        if (state.value.sortType == ShoppingItemSort.MANUAL) {
            state.update { it.copy(items = visibleManualItems()) }
        }
    }

    private fun resolveItemsForSave(orderIds: List<Long>): List<ShoppingItem> =
        mergeItemsWithOrder(orderIds, latestManualItems)

    private fun visibleManualItems(): List<ShoppingItem> = preferredOrderIds?.let { orderIds ->
        mergeItemsWithOrder(orderIds, latestManualItems)
    } ?: latestManualItems
}

internal class ProductSuggestionLoader(
    private val scope: CoroutineScope,
    private val interactor: ShoppingItemInteractor,
    private val onLoaded: (String, List<String>) -> Unit,
) {
    private var job: Job? = null

    fun request(query: String) {
        cancel()
        if (query.isBlank()) return

        job = scope.launch {
            delay(SUGGESTIONS_DEBOUNCE_MILLIS)
            try {
                val suggestions = interactor.findProductSuggestions(
                    query = query,
                    limit = SUGGESTIONS_LIMIT,
                ).distinctBy { suggestion ->
                    suggestion.lowercase(Locale.ROOT)
                }
                onLoaded(query, suggestions)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Suggestions are optional; input remains fully usable on failure.
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}

internal data class PositionSaveCallbacks(
    val onSaved: (List<Long>) -> Unit,
    val onFailure: () -> Unit,
    val onIdle: () -> Unit,
)

internal class ItemPositionUpdateQueue(
    private val scope: CoroutineScope,
    private val interactor: ShoppingItemInteractor,
    private val resolveItems: (List<Long>) -> List<ShoppingItem>,
    private val callbacks: PositionSaveCallbacks,
) {
    private var saveJob: Job? = null
    private var pendingOrderIds: List<Long>? = null

    val isSaving: Boolean
        get() = saveJob?.isActive == true

    fun enqueue(orderIds: List<Long>) {
        pendingOrderIds = orderIds
        if (isSaving) return

        saveJob = scope.launch {
            drainQueue()
        }
    }

    private suspend fun drainQueue() {
        while (true) {
            val orderIds = pendingOrderIds ?: break
            pendingOrderIds = null

            try {
                interactor.updatePositions(resolveItems(orderIds))
                callbacks.onSaved(orderIds)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                pendingOrderIds = null
                callbacks.onFailure()
                return
            }
        }
        callbacks.onIdle()
    }
}

private fun mergeItemsWithOrder(
    orderIds: List<Long>,
    latestItems: List<ShoppingItem>,
): List<ShoppingItem> {
    val itemsById = latestItems.associateBy(ShoppingItem::id)
    return mergeOrderIds(orderIds, latestItems).mapIndexedNotNull { position, id ->
        itemsById[id]?.copy(position = position)
    }
}

private fun mergeOrderIds(
    orderIds: List<Long>,
    latestItems: List<ShoppingItem>,
): List<Long> {
    val latestIds = latestItems.map(ShoppingItem::id)
    val latestIdSet = latestIds.toHashSet()
    val orderedExistingIds = orderIds.filterTo(mutableListOf()) { it in latestIdSet }
    val orderedIdSet = orderedExistingIds.toHashSet()
    return orderedExistingIds + latestIds.filterNot { it in orderedIdSet }
}

private const val SUGGESTIONS_DEBOUNCE_MILLIS = 300L
private const val SUGGESTIONS_LIMIT = 5
