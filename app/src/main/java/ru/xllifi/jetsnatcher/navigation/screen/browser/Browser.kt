package ru.xllifi.jetsnatcher.navigation.screen.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class BrowserNavKey : NavKey

// TODO: rewrite using nav3 scenes
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Browser(
  viewModel: BrowserViewModel = viewModel(),
  innerPadding: PaddingValues,
  postViewToolbarActions: PostToolbarActions = PostToolbarActions(),
  onSearchClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  if (!uiState.expandPost && uiState.searches.size > 1) {
    BackHandler { viewModel.prevSearch() }
  }

  SharedTransitionLayout {
    Column {
      PostGrid(
        modifier = Modifier.weight(1f),
        browserViewModel = viewModel,
        innerPadding = innerPadding,
        onScrolledToBottom = {
          GlobalScope.launch {
            viewModel.loadPosts(context)
          }
        }
      )
      AnimatedVisibility(
        visible = !uiState.expandPost
      ) {
        BottomBar(
          modifier = Modifier
            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 5f)
            .animateEnterExit(
              exit = slideOut { size -> IntOffset(x = 0, y = size.height) } + fadeOut(),
              enter = slideIn { size -> IntOffset(x = 0, y = size.height) },
            ),
          innerPadding = innerPadding,
          onClick = onSearchClick,
        )
      }
    }
    AnimatedVisibility(
      visible = uiState.expandPost,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      PostView(
        browserViewModel = viewModel,
        animatedVisibilityScope = this,
        onBack = { viewModel.expandPost(to = false) },
        onSelectedPostChange = { viewModel.selectPost(it) },
        innerPadding = innerPadding,
        postToolbarActions = postViewToolbarActions,
      )
    }
  }
}