package ru.practicum.shoppinglist.presentation.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
internal fun LoginContent(
    state: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (LoginIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            Spacer(Modifier.height(56.dp))
            LoginHeader()
            Spacer(Modifier.height(32.dp))
            LoginForm(state = state, onIntent = onIntent)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LoginHeader() {
    Surface(
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = stringResource(R.string.login_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoginForm(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoginEmailField(state = state, onIntent = onIntent)
        LoginPasswordField(state = state, onIntent = onIntent)

        Spacer(Modifier.height(8.dp))

        LoginButton(state = state, onIntent = onIntent)

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { onIntent(LoginIntent.RegistrationClicked) },
        ) {
            Text(stringResource(R.string.registration_action))
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { onIntent(LoginIntent.PasswordRecoveryClicked) },
        ) {
            Text(stringResource(R.string.password_recovery_action))
        }
    }
}

@Composable
private fun LoginEmailField(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
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
private fun LoginPasswordField(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    OutlinedTextField(
        value = state.password,
        onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
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
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (state.isLoginEnabled) {
                    onIntent(LoginIntent.LoginClicked)
                }
            },
        ),
        trailingIcon = {
            PasswordVisibilityButton(state = state, onIntent = onIntent)
        },
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun PasswordVisibilityButton(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    val description = if (state.isPasswordVisible) {
        R.string.auth_hide_password
    } else {
        R.string.auth_show_password
    }

    IconButton(
        onClick = { onIntent(LoginIntent.PasswordVisibilityClicked) },
    ) {
        Icon(
            imageVector = if (state.isPasswordVisible) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            },
            contentDescription = stringResource(description),
        )
    }
}

@Composable
private fun LoginButton(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = state.isLoginEnabled,
        onClick = { onIntent(LoginIntent.LoginClicked) },
        shape = MaterialTheme.shapes.large,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(stringResource(R.string.login_action))
        }
    }
}
