package ru.practicum.shoppinglist.presentation.auth.check

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AuthCheckScreen(
    onAuthenticated: () -> Unit,
    onAuthenticationRequired: () -> Unit,
    viewModel: AuthCheckViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthCheckEffect.NavigateToLogin -> {
                    onAuthenticationRequired()
                }

                AuthCheckEffect.NavigateToShoppingLists -> {
                    onAuthenticated()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
