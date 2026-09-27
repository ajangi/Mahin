package dev.mahin.android.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.fillMaxSize().padding(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        Text(text = stringResource(R.string.learn_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.learn_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.learn_search_label)) },
            singleLine = true,
        )
        TextButton(onClick = viewModel::search) {
            Text(stringResource(R.string.learn_search_action))
        }
        if (state.loading) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null && state.results.isEmpty()) {
            MahinEmptyState(
                title = stringResource(R.string.learn_offline_title),
                body = stringResource(R.string.learn_offline_body),
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
            items(state.results, key = { it.id }) { item ->
                val bookmarked = item.id in state.localBookmarks
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                    item.summary?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { viewModel.toggleBookmark(item.id) }) {
                        Icon(
                            imageVector = if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.learn_bookmark_toggle),
                        )
                    }
                }
            }
        }
    }
}
