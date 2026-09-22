package ru.practicum.shoppinglist.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Message

@Composable
internal fun ShoppingListEditorErrorContent(
    error: Message,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(error.stringResource),
            style = MaterialTheme.typography.titleMedium,
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetry,
        ) {
            Text(text = stringResource(R.string.editor_retry))
        }
    }
}
