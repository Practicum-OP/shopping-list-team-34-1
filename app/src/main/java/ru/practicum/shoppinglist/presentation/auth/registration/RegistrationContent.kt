package ru.practicum.shoppinglist.presentation.auth.registration

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R

@Composable
internal fun RegistrationContent(
    state: RegistrationUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RegistrationTopBar(
                enabled = !state.isLoading,
                onBack = { onIntent(RegistrationIntent.BackClicked) },
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
                text = stringResource(R.string.registration_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            RegistrationForm(
                state = state,
                onIntent = onIntent,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationTopBar(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(R.string.registration_title))
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
private fun RegistrationForm(
    state: RegistrationUiState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RegistrationEmailField(state = state, onIntent = onIntent)
        RegistrationPasswordField(state = state, onIntent = onIntent)
        RepeatedPasswordField(state = state, onIntent = onIntent)

        Spacer(Modifier.height(8.dp))

        RegistrationButton(state = state, onIntent = onIntent)
    }
}

@Composable
private fun RegistrationEmailField(
    state: RegistrationUiState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = {
            onIntent(RegistrationIntent.EmailChanged(it))
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
            imeAction = ImeAction.Next,
        ),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun RegistrationPasswordField(
    state: RegistrationUiState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.password,
        onValueChange = {
            onIntent(RegistrationIntent.PasswordChanged(it))
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        label = { Text(stringResource(R.string.auth_password_label)) },
        singleLine = true,
        isError = state.showPasswordError,
        supportingText = if (state.showPasswordError) {
            { Text(stringResource(R.string.auth_password_error)) }
        } else {
            null
        },
        visualTransformation = if (state.isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
        ),
        trailingIcon = {
            PasswordVisibilityButton(
                isVisible = state.isPasswordVisible,
                onClick = {
                    onIntent(
                        RegistrationIntent.PasswordVisibilityClicked,
                    )
                },
            )
        },
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun RepeatedPasswordField(
    state: RegistrationUiState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.repeatedPassword,
        onValueChange = {
            onIntent(RegistrationIntent.RepeatedPasswordChanged(it))
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        label = {
            Text(stringResource(R.string.auth_repeat_password_label))
        },
        singleLine = true,
        isError = state.showRepeatedPasswordError,
        supportingText = if (state.showRepeatedPasswordError) {
            { Text(stringResource(R.string.auth_passwords_mismatch)) }
        } else {
            null
        },
        visualTransformation = if (state.isRepeatedPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (state.isRegistrationEnabled) {
                    onIntent(RegistrationIntent.RegistrationClicked)
                }
            },
        ),
        trailingIcon = {
            PasswordVisibilityButton(
                isVisible = state.isRepeatedPasswordVisible,
                onClick = {
                    onIntent(
                        RegistrationIntent.RepeatedPasswordVisibilityClicked,
                    )
                },
            )
        },
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun PasswordVisibilityButton(
    isVisible: Boolean,
    onClick: () -> Unit,
) {
    val description = if (isVisible) {
        R.string.auth_hide_password
    } else {
        R.string.auth_show_password
    }

    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isVisible) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            },
            contentDescription = stringResource(description),
        )
    }
}

@Composable
private fun RegistrationButton(
    state: RegistrationUiState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = state.isRegistrationEnabled,
        onClick = {
            onIntent(RegistrationIntent.RegistrationClicked)
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
            Text(stringResource(R.string.registration_action))
        }
    }
}
