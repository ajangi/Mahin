package dev.mahin.android.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LearnScreenContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onToggleBookmark = viewModel::toggleBookmark,
        modifier = modifier,
    )
}
