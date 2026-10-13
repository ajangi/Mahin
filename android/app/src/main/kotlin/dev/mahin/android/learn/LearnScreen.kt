package dev.mahin.android.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
    illustrationViewModel: LearnIllustrationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "learn/home",
        modifier = modifier,
    ) {
        composable("learn/home") {
            LearnScreenContent(
                state = state,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::search,
                onToggleBookmark = viewModel::toggleBookmark,
                onSelectCategory = viewModel::selectCategory,
                onOpenArticle = { id -> navController.navigate("learn/article/$id") },
                onOpenProgram = { id -> navController.navigate("learn/program/$id") },
                onOpenCollection = { id -> navController.navigate("learn/article/$id") },
                illustrationSource = illustrationViewModel.illustrationSource,
            )
        }
        composable(
            route = "learn/article/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
        ) {
            val articleViewModel: ArticleViewModel = hiltViewModel()
            val articleState by articleViewModel.uiState.collectAsStateWithLifecycle()
            ArticleScreenContent(state = articleState)
        }
        composable(
            route = "learn/program/{programId}",
            arguments = listOf(navArgument("programId") { type = NavType.StringType }),
        ) {
            val programViewModel: ProgramDetailViewModel = hiltViewModel()
            val programState by programViewModel.uiState.collectAsStateWithLifecycle()
            ProgramDetailScreenContent(
                state = programState,
                onStartSession = { session ->
                    val id = programState.program?.id ?: return@ProgramDetailScreenContent
                    navController.navigate("learn/play/$id/${session.id}")
                },
            )
        }
        composable(
            route = "learn/play/{programId}/{sessionId}",
            arguments =
                listOf(
                    navArgument("programId") { type = NavType.StringType },
                    navArgument("sessionId") { type = NavType.StringType },
                ),
        ) {
            val playerViewModel: GuidedPlayerViewModel = hiltViewModel()
            val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
            GuidedPlayerScreenContent(
                state = playerState,
                onPause = playerViewModel::pause,
                onResume = playerViewModel::resume,
                onComplete = playerViewModel::completeSession,
            )
        }
    }
}
