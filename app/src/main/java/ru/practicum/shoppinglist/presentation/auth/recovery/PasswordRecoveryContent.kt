package ru.practicum.shoppinglist.presentation.auth.recovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R

@Composable
internal fun PasswordRecoveryContent(
    state: PasswordRecoveryUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PasswordRecoveryIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PasswordRecoveryTopBar(
                enabled = !state.isLoading,
                onBack = {
                    onIntent(PasswordRecoveryIntent.BackClicked)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(
                    R.string.password_recovery_subtitle,
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            RecoveryEmailField(state = state, onIntent = onIntent)

            Spacer(Modifier.height(24.dp))

            RecoverySubmitButton(state = state, onIntent = onIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordRecoveryTopBar(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(R.string.password_recovery_title))
        },
        navigationIcon = {
            IconButton(
                enabled = enabled,
                onClick = onBack,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        R.string.auth_navigate_back,
                    ),
                )
            }
        },
    )
}

@Composable
private fun RecoveryEmailField(
    state: PasswordRecoveryUiState,
    onIntent: (PasswordRecoveryIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = {
            onIntent(PasswordRecoveryIntent.EmailChanged(it))
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        label = { Text(stringResource(R.string.auth_email_label)) },
        singleLine = true,
        isError = state.showEmailError,
        supportingText = if (state.showEmailError) {
            { Text(stringResource(R.string.auth_email_error)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (state.isSubmitEnabled) {
                    onIntent(PasswordRecoveryIntent.SubmitClicked)
                }
            },
        ),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun RecoverySubmitButton(
    state: PasswordRecoveryUiState,
    onIntent: (PasswordRecoveryIntent) -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = state.isSubmitEnabled,
        onClick = {
            onIntent(PasswordRecoveryIntent.SubmitClicked)
        },
        shape = MaterialTheme.shapes.large,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(
                    R.string.password_recovery_submit,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}
