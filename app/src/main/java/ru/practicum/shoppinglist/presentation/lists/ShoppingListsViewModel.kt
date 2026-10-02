package ru.practicum.shoppinglist.presentation.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.model.ShoppingList
import ru.practicum.shoppinglist.domain.api.repository.AuthRepository

internal class ShoppingListsViewModel(
    private val interactor: ShoppingListInteractor,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ShoppingListsUiState())
    val state = mutableState.asStateFlow()

    private val effectChannel = Channel<ShoppingListsEffect>(Channel.BUFFERED)
    val effects: Flow<ShoppingListsEffect> = effectChannel.receiveAsFlow()

    init {
        observeShoppingLists()
    }

    fun onAction(action: ShoppingListsAction) {
        when (action) {
            ShoppingListsAction.CreateListClicked -> mutableState.update { state ->
                state.copy(createDialog = CreateListDialogState())
            }
            ShoppingListsAction.CreateDialogDismissed -> dismissCreateDialog()
            is ShoppingListsAction.CreateNameChanged -> changeCreateName(action.name)
            is ShoppingListsAction.CreateIconSelected -> selectCreateIcon(action.iconKey)
            ShoppingListsAction.CreateConfirmed -> createList()
            is ShoppingListsAction.RenameListClicked -> {
                mutableState.showRenameDialog(action.shoppingList)
            }
            ShoppingListsAction.RenameDialogDismissed -> {
                mutableState.dismissRenameDialog()
            }
            is ShoppingListsAction.RenameNameChanged -> {
                mutableState.changeRenameName(action.name)
            }
            is ShoppingListsAction.RenameIconSelected -> {
                mutableState.selectRenameIcon(action.iconKey)
            }
            ShoppingListsAction.RenameConfirmed -> renameList()
            is ShoppingListsAction.DeleteListClicked -> requestDeletion(action.shoppingList)
            ShoppingListsAction.DeleteDialogDismissed -> dismissDeleteDialog()
            ShoppingListsAction.DeleteConfirmed -> deleteList()
            ShoppingListsAction.LogoutClicked -> logout()
        }
    }

    private fun observeShoppingLists() {
        viewModelScope.launch {
            interactor.observeShoppingLists()
                .catch {
                    mutableState.update { state ->
                        state.copy(isLoading = false, loadFailed = true)
                    }
                }
                .collect { shoppingLists ->
                    mutableState.update { state ->
                        state.copy(
                            lists = shoppingLists,
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
    }

    private fun dismissCreateDialog() {
        mutableState.updateUnlessSubmitting { state -> state.copy(createDialog = null) }
    }

    private fun changeCreateName(name: String) {
        mutableState.update { state ->
            state.copy(
                createDialog = state.createDialog?.copy(
                    name = name,
                    showNameError = false,
                ),
            )
        }
    }

    private fun selectCreateIcon(iconKey: String) {
        val knownIconKey = ShoppingListIconRegistry.resolve(iconKey).key
        mutableState.update { state ->
            state.copy(createDialog = state.createDialog?.copy(iconKey = knownIconKey))
        }
    }

    private fun createList() {
        val currentState = mutableState.value
        val dialog = currentState.createDialog
        if (currentState.isSubmitting || dialog == null) return
        val preparedName = dialog.name.trim()
        if (preparedName.isEmpty()) {
            mutableState.update { state ->
                state.copy(createDialog = dialog.copy(showNameError = true))
            }
        } else {
            mutableState.update { state -> state.copy(isSubmitting = true) }
            viewModelScope.launch {
                runCatching {
                    interactor.createShoppingList(preparedName, dialog.iconKey)
                }.onSuccess { createdListId ->
                    if (createdListId == null) {
                        onOperationFailed()
                    } else {
                        mutableState.update { state ->
                            state.copy(createDialog = null, isSubmitting = false)
                        }
                    }
                }.onFailure { onOperationFailed() }
            }
        }
    }

    private fun renameList() {
        val currentState = mutableState.value
        val dialog = currentState.renameDialog
        if (currentState.isSubmitting || dialog == null) return
        val preparedName = dialog.name.trim()
        if (preparedName.isEmpty()) {
            mutableState.update { state ->
                state.copy(renameDialog = dialog.copy(showNameError = true))
            }
            return
        }

        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            val updatedList = dialog.shoppingList.copy(
                name = preparedName,
                iconKey = dialog.iconKey,
            )
            runCatching { interactor.updateShoppingList(updatedList) }
                .onSuccess { isUpdated ->
                    if (isUpdated) {
                        mutableState.update { state ->
                            state.copy(renameDialog = null, isSubmitting = false)
                        }
                    } else {
                        onOperationFailed()
                    }
                }
                .onFailure { onOperationFailed() }
        }
    }

    private fun requestDeletion(shoppingList: ShoppingList) {
        mutableState.update { state -> state.copy(listPendingDeletion = shoppingList) }
    }

    private fun dismissDeleteDialog() {
        mutableState.updateUnlessSubmitting { state -> state.copy(listPendingDeletion = null) }
    }

    private fun deleteList() {
        val currentState = mutableState.value
        if (currentState.isSubmitting) return
        val shoppingList = currentState.listPendingDeletion ?: return
        mutableState.update { state -> state.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching { interactor.deleteShoppingList(shoppingList.id) }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(listPendingDeletion = null, isSubmitting = false)
                    }
                }
                .onFailure { onOperationFailed() }
        }
    }

    private fun onOperationFailed() {
        mutableState.update { state -> state.copy(isSubmitting = false) }
        effectChannel.trySend(ShoppingListsEffect.OperationFailed)
    }

    private fun logout() {
        val currentState = mutableState.value

        if (currentState.isLoggingOut) {
            return
        }

        mutableState.update { state ->
            state.copy(isLoggingOut = true)
        }

        viewModelScope.launch {
            val result = runCatching {
                authRepository.logout()
            }

            mutableState.update { state ->
                state.copy(isLoggingOut = false)
            }

            if (result.isSuccess) {
                effectChannel.send(
                    ShoppingListsEffect.NavigateToLogin,
                )
            } else {
                effectChannel.send(
                    ShoppingListsEffect.OperationFailed,
                )
            }
        }
    }

}

private inline fun MutableStateFlow<ShoppingListsUiState>.updateUnlessSubmitting(
    transform: (ShoppingListsUiState) -> ShoppingListsUiState,
) {
    update { state -> if (state.isSubmitting) state else transform(state) }
}

private fun MutableStateFlow<ShoppingListsUiState>.showRenameDialog(
    shoppingList: ShoppingList,
) {
    update { state ->
        state.copy(renameDialog = RenameListDialogState(shoppingList))
    }
}

private fun MutableStateFlow<ShoppingListsUiState>.dismissRenameDialog() {
    updateUnlessSubmitting { state -> state.copy(renameDialog = null) }
}

private fun MutableStateFlow<ShoppingListsUiState>.changeRenameName(name: String) {
    update { state ->
        state.copy(
            renameDialog = state.renameDialog?.copy(
                name = name,
                showNameError = false,
            ),
        )
    }
}

private fun MutableStateFlow<ShoppingListsUiState>.selectRenameIcon(iconKey: String) {
    val knownIconKey = ShoppingListIconRegistry.resolve(iconKey).key
    update { state ->
        state.copy(renameDialog = state.renameDialog?.copy(iconKey = knownIconKey))
    }
}
