package ru.practicum.shoppinglist.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingItemSort
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Dialog
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Effect
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Intent
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Message
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.State
import ru.practicum.shoppinglist.presentation.lists.ShoppingListIconRegistry

internal class ShoppingListEditorViewModel(
    private val listId: Long,
    private val shoppingListInteractor: ShoppingListInteractor,
    private val shoppingItemInteractor: ShoppingItemInteractor,
    private val sortStorage: ShoppingItemSortStorage,
) : ViewModel() {

    private val selectedSort = MutableStateFlow(sortStorage.get(listId))
    private val _state = MutableStateFlow(
        State(sort = selectedSort.value),
    )
    val state: StateFlow<State> = _state.asStateFlow()

    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var shoppingList: ShoppingList? = null
    private var itemsJob: Job? = null

    init {
        loadContent()
    }

    @Suppress("CyclomaticComplexMethod")
    fun onIntent(intent: Intent) {
        if (_state.value.isDeletingList) return

        when (intent) {
            Intent.AddItemClicked -> _state.showItemForm()
            is Intent.EditItemClicked -> _state.showItemForm(intent.item)
            is Intent.DeleteItemClicked -> _state.showDialog(Dialog.DeleteItem(intent.item))
            is Intent.PurchasedChanged -> setPurchased(intent)
            is Intent.SortChanged -> _state.changeEditorSort(
                listId = listId,
                sort = intent.sort,
                selectedSort = selectedSort,
                sortStorage = sortStorage,
            )
            Intent.RenameListClicked -> _state.showRenameList(
                name = _state.value.listName,
                iconKey = shoppingList?.iconKey,
            )
            Intent.DeleteListClicked -> _state.showDialog(Dialog.DeleteList)
            Intent.ClearPurchasedClicked -> _state.showDialog(Dialog.ClearPurchased)
            is Intent.ItemNameChanged -> _state.changeItemName(intent.value)
            is Intent.ItemQuantityChanged -> _state.changeItemQuantity(intent.value)
            is Intent.ItemUnitChanged -> _state.changeItemUnit(intent.value)
            is Intent.ListNameChanged -> _state.changeListName(intent.value)
            is Intent.ListIconChanged -> _state.changeListIcon(intent.iconKey)
            Intent.SaveItemClicked -> saveItem()
            Intent.SaveListNameClicked -> saveListName()
            Intent.ConfirmDeleteItemClicked -> deleteItem()
            Intent.ConfirmDeleteListClicked -> deleteList()
            Intent.ConfirmClearPurchasedClicked -> clearPurchased()
            Intent.DialogDismissed -> _state.dismissDialog()
            Intent.MessageShown -> _state.clearMessage()
            Intent.RetryClicked -> loadContent()
        }
    }

    private fun loadContent() {
        _state.update { state ->
            state.copy(
                isLoading = true,
                loadError = null,
                message = null,
            )
        }
        loadShoppingList()
        observeShoppingItems()
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            runCatching {
                shoppingListInteractor.getShoppingList(listId)
            }.onSuccess { loadedList ->
                shoppingList = loadedList
                _state.update { state ->
                    state.copy(
                        listName = loadedList?.name.orEmpty(),
                        loadError = if (loadedList == null) {
                            Message.LIST_NOT_FOUND
                        } else {
                            null
                        },
                    )
                }
            }.onFailure {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        loadError = Message.LOAD_FAILED,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeShoppingItems() {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            selectedSort
                .flatMapLatest { sort ->
                    shoppingItemInteractor.observeShoppingItems(
                        listId = listId,
                        sort = sort,
                    )
                }
                .catch {
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            loadError = Message.LOAD_FAILED,
                        )
                    }
                }
                .collect { items ->
                    _state.update { state ->
                        state.copy(
                            items = items,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun setPurchased(intent: Intent.PurchasedChanged) {
        viewModelScope.launch {
            runCatching {
                shoppingItemInteractor.setPurchased(
                    itemId = intent.itemId,
                    isPurchased = intent.isPurchased,
                )
            }.onFailure {
                _state.showMessage(Message.SAVE_FAILED)
            }
        }
    }

    private fun saveItem() {
        val form = _state.value.dialog as? Dialog.ItemForm ?: return
        if (form.isSaving) return

        val preparedName = form.name.trim()
        val quantity = form.quantity.toQuantityOrNull()
        val hasQuantityError = form.quantity.isNotBlank() && quantity == null
        if (preparedName.isBlank() || hasQuantityError) {
            _state.showDialog(
                form.copy(
                    nameError = preparedName.isBlank(),
                    quantityError = hasQuantityError,
                ),
            )
        } else {
            _state.showDialog(form.copy(isSaving = true))
            viewModelScope.launch {
                val isSaved = runCatching {
                    shoppingItemInteractor.persistItem(
                        listId = listId,
                        form = form,
                        name = preparedName,
                        quantity = quantity,
                    )
                }.getOrDefault(false)

                if (isSaved) {
                    _state.dismissDialog()
                } else {
                    _state.showDialog(form.copy(isSaving = false))
                    _state.showMessage(Message.SAVE_FAILED)
                }
            }
        }
    }

    private fun saveListName() {
        val dialog = _state.value.dialog as? Dialog.RenameList ?: return
        val currentList = shoppingList
        val preparedName = dialog.name.trim()
        when {
            currentList == null || dialog.isSaving -> Unit
            preparedName.isBlank() -> {
                _state.showDialog(dialog.copy(hasError = true))
            }
            else -> {
                _state.showDialog(dialog.copy(isSaving = true))
                viewModelScope.launch {
                    val updatedList = currentList.copy(
                        name = preparedName,
                        iconKey = ShoppingListIconRegistry.resolve(dialog.iconKey).key,
                    )
                    val isSaved = runCatching {
                        shoppingListInteractor.updateShoppingList(updatedList)
                    }.getOrDefault(false)

                    if (isSaved) {
                        shoppingList = updatedList
                        _state.update { state ->
                            state.copy(listName = preparedName, dialog = null)
                        }
                    } else {
                        _state.showDialog(dialog.copy(isSaving = false))
                        _state.showMessage(Message.SAVE_FAILED)
                    }
                }
            }
        }
    }

    private fun deleteItem() {
        val dialog = _state.value.dialog as? Dialog.DeleteItem ?: return
        _state.dismissDialog()
        viewModelScope.launch {
            runCatching {
                shoppingItemInteractor.deleteShoppingItem(dialog.item.id)
            }.onFailure {
                _state.showMessage(Message.SAVE_FAILED)
            }
        }
    }

    private fun deleteList() {
        if (_state.value.dialog !is Dialog.DeleteList) return
        _state.update { state ->
            state.copy(
                dialog = null,
                isDeletingList = true,
            )
        }
        viewModelScope.launch {
            runCatching {
                shoppingListInteractor.deleteShoppingList(listId)
            }.onSuccess {
                sortStorage.remove(listId)
                effectChannel.send(Effect.NavigateBack)
            }.onFailure {
                _state.update { state -> state.copy(isDeletingList = false) }
                _state.showMessage(Message.SAVE_FAILED)
            }
        }
    }

    private fun clearPurchased() {
        if (_state.value.dialog !is Dialog.ClearPurchased) return
        _state.dismissDialog()
        viewModelScope.launch {
            runCatching {
                shoppingItemInteractor.deletePurchasedItems(listId)
            }.onFailure {
                _state.showMessage(Message.SAVE_FAILED)
            }
        }
    }
}
