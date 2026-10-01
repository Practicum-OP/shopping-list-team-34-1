package ru.practicum.shoppinglist.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.model.MeasurementUnit
import ru.practicum.shoppinglist.domain.api.model.ShoppingItem
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort

internal class ListEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val interactor: ShoppingItemInteractor,
) : ViewModel() {

    private val listId: Long = savedStateHandle.get<Long>("listId") ?: 0L
    private val listName: String = savedStateHandle.get<String>("listName")?.let {
        java.net.URLDecoder.decode(it, "UTF-8")
    } ?: ""

    private val mutableState = MutableStateFlow(ListEditorUiState(listName = listName))
    val state = mutableState.asStateFlow()

    private val effectChannel = Channel<ListEditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var suggestionsJob: kotlinx.coroutines.Job? = null

    init {
        observeShoppingItems(ShoppingItemSort.MANUAL)
    }

    fun onIntent(intent: ListEditorIntent) {
        when (intent) {
            is ListEditorIntent.AddItemClicked -> handleAddItemClicked()
            is ListEditorIntent.NameChanged -> handleNameChanged(intent.name)
            is ListEditorIntent.QuantityChanged -> handleQuantityChanged(intent.quantity)
            is ListEditorIntent.UnitChanged -> handleUnitChanged(intent.unit)
            is ListEditorIntent.ConfirmAddItem -> handleConfirmAddItem()
            is ListEditorIntent.SuggestionSelected -> handleSuggestionSelected(intent.name)
            is ListEditorIntent.ItemEdited -> editItem(intent.item)
            is ListEditorIntent.ItemDeleted -> deleteItem(intent.itemId)
            is ListEditorIntent.ItemEditRequested -> handleItemEditRequested(intent.itemId)
            is ListEditorIntent.DeleteCancelled -> dismissDeleteDialog()
            is ListEditorIntent.ItemToggled -> toggleItem(intent.itemId)
            is ListEditorIntent.DeleteItemRequested -> requestDeleteItem(intent.itemId)
            is ListEditorIntent.ClearListClicked -> requestClearList()
            is ListEditorIntent.ClearConfirmed -> clearList()
            is ListEditorIntent.ClearCancelled -> dismissClearDialog()
            is ListEditorIntent.SortChanged -> handleSortChanged(intent.sortType)
            is ListEditorIntent.ToggleSortMenu -> toggleSortMenu()
            is ListEditorIntent.ItemMoved -> handleItemMoved(intent.fromIndex, intent.toIndex)
        }
    }

    private fun requestDeleteItem(itemId: Long) {
        val item = mutableState.value.items.find { it.id == itemId } ?: return
        mutableState.update { state ->
            state.copy(listPendingDeletion = item)
        }
    }

    private fun handleItemEditRequested(itemId: Long) {
        val item = mutableState.value.items.find { it.id == itemId } ?: return
        mutableState.update { state ->
            state.copy(
                createDialog = CreateItemDialogState(
                    name = item.name,
                    quantity = item.quantity?.toInt() ?: 1,
                    selectedUnit = item.unit ?: MeasurementUnit.PIECE,
                    isEditing = true,
                    id = item.id
                )
            )
        }
    }

    private fun observeShoppingItems(sort: ShoppingItemSort) {
        viewModelScope.launch {
            interactor.observeShoppingItems(listId = listId, sort = sort)
                .catch {
                    mutableState.update { it.copy(isSubmitting = false) }
                    effectChannel.trySend(ListEditorEffect.OperationFailed)
                }
                .collect { items ->
                    mutableState.update { it.copy(items = items, isSubmitting = false) }
                }
        }
    }

    private fun handleAddItemClicked() {
        mutableState.update { state ->
            state.copy(
                createDialog = if (state.createDialog == null) {
                    CreateItemDialogState()
                } else {
                    null
                }
            )
        }
    }

    private fun dismissCreateDialog() {
        mutableState.updateUnlessSubmitting { state -> state.copy(createDialog = null) }
    }

    private fun handleNameChanged(name: String) {
        mutableState.update { state ->
            state.copy(
                createDialog = state.createDialog?.copy(
                    name = name,
                    showNameError = false,
                    suggestions = emptyList()
                )
            )
        }
        loadSuggestions(name)
    }

    private fun loadSuggestions(query: String) {
        suggestionsJob?.cancel()
        if (query.isBlank()) return

        suggestionsJob = viewModelScope.launch {
            delay(300)
            runCatching {
                interactor.findProductSuggestions(query, limit = 5)
            }.onSuccess { suggestions ->
                mutableState.update { state ->
                    state.copy(
                        createDialog = state.createDialog?.copy(suggestions = suggestions)
                    )
                }
            }
        }
    }

    private fun handleSuggestionSelected(name: String) {
        mutableState.update { state ->
            state.copy(
                createDialog = state.createDialog?.copy(
                    name = name,
                    suggestions = emptyList(),
                    showNameError = false
                )
            )
        }
    }

    private fun handleQuantityChanged(quantity: Int) {
        mutableState.update { state ->
            state.copy(
                createDialog = state.createDialog?.copy(
                    quantity = quantity,
                    showQuantityError = false
                )
            )
        }
    }

    private fun handleUnitChanged(unit: MeasurementUnit) {
        mutableState.update { state ->
            state.copy(
                createDialog = state.createDialog?.copy(selectedUnit = unit)
            )
        }
    }

    private fun handleConfirmAddItem() {
        val dialog = mutableState.value.createDialog ?: return

        val hasNameError = dialog.name.isBlank()
        val hasQuantityError = dialog.quantity <= 0

        if (hasNameError || hasQuantityError) {
            mutableState.update { state ->
                state.copy(
                    createDialog = dialog.copy(
                        showNameError = hasNameError,
                        showQuantityError = hasQuantityError,
                    ),
                )
            }
            return
        }

        if (dialog.isEditing) {
            val itemId = dialog.id ?: return
            val item = mutableState.value.items.find { it.id == itemId }
                ?: return

            editItem(
                item.copy(
                    name = dialog.name,
                    quantity = dialog.quantity.toDouble(),
                    unit = dialog.selectedUnit,
                ),
            )
        } else {
            addItem(
                name = dialog.name,
                quantity = dialog.quantity,
                unit = dialog.selectedUnit,
            )
        }
    }

    private fun addItem(
        name: String,
        quantity: Int,
        unit: MeasurementUnit,
    ) {
        if (name.isBlank() || quantity <= 0) {
            return
        }

        mutableState.update { state ->
            state.copy(isSubmitting = true)
        }

        viewModelScope.launch {
            val itemId = runCatching {
                interactor.addShoppingItem(
                    listId = listId,
                    name = name,
                    quantity = quantity.toDouble(),
                    unit = unit,
                )
            }.getOrNull()

            if (itemId != null) {
                mutableState.update { state ->
                    state.copy(
                        createDialog = null,
                        isSubmitting = false,
                    )
                }
            } else {
                mutableState.update { state ->
                    state.copy(isSubmitting = false)
                }
                effectChannel.trySend(ListEditorEffect.OperationFailed)
            }
        }
    }

    private fun editItem(item: ShoppingItem) {
        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching {
                interactor.updateShoppingItem(item)
            }.onSuccess {
                mutableState.update { state -> state.copy(createDialog = null, isSubmitting = false) }
            }.onFailure {
                mutableState.update { state -> state.copy(isSubmitting = false) }
                effectChannel.trySend(ListEditorEffect.OperationFailed)
            }
        }
    }

    private fun deleteItem(itemId: Long) {
        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching {
                interactor.deleteShoppingItem(itemId)
            }.onSuccess {
                mutableState.update { state ->
                    state.copy(isSubmitting = false, listPendingDeletion = null)
                }
            }.onFailure {
                mutableState.update { state -> state.copy(isSubmitting = false) }
                effectChannel.trySend(ListEditorEffect.OperationFailed)
            }
        }
    }

    private fun dismissDeleteDialog() {
        mutableState.updateUnlessSubmitting { state -> state.copy(listPendingDeletion = null) }
    }

    private fun toggleItem(itemId: Long) {
        val currentItem = mutableState.value.items.find { it.id == itemId } ?: return
        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching {
                interactor.setPurchased(
                    itemId = itemId,
                    isPurchased = !currentItem.isPurchased
                )
            }.onSuccess {
                mutableState.update { state -> state.copy(isSubmitting = false) }
            }.onFailure {
                mutableState.update { state -> state.copy(isSubmitting = false) }
                effectChannel.trySend(ListEditorEffect.OperationFailed)
            }
        }
    }

    private fun requestClearList() {
        mutableState.update { state -> state.copy(showClearDialog = true) }
    }

    private fun dismissClearDialog() {
        mutableState.updateUnlessSubmitting { state -> state.copy(showClearDialog = false) }
    }

    private fun clearList() {
        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching {
                interactor.deletePurchasedItems(listId = listId)
            }.onSuccess {
                mutableState.update { state ->
                    state.copy(isSubmitting = false, showClearDialog = false)
                }
            }.onFailure {
                mutableState.update { state -> state.copy(isSubmitting = false) }
                effectChannel.trySend(ListEditorEffect.OperationFailed)
            }
        }
    }

    private fun handleSortChanged(sortType: ShoppingItemSort) {
        mutableState.update { it.copy(sortType = sortType) }
        observeShoppingItems(sortType)
    }

    private fun toggleSortMenu() {
        mutableState.update { it.copy(showSortMenu = !it.showSortMenu) }
    }

    private fun handleItemMoved(fromIndex: Int, toIndex: Int) {
        val currentItems = mutableState.value.items.toMutableList()
        if (fromIndex in currentItems.indices && toIndex in currentItems.indices) {
            val item = currentItems.removeAt(fromIndex)
            currentItems.add(toIndex, item)
            mutableState.update { it.copy(items = currentItems) }
            viewModelScope.launch {
                runCatching {
                    interactor.updatePositions(currentItems)
                }.onFailure {
                    effectChannel.trySend(ListEditorEffect.OperationFailed)
                }
            }
        }
    }
}

private inline fun MutableStateFlow<ListEditorUiState>.updateUnlessSubmitting(
    transform: (ListEditorUiState) -> ListEditorUiState,
) {
    update { state -> if (state.isSubmitting) state else transform(state) }
}