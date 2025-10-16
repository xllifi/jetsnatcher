package ru.xllifi.jetsnatcher.navigation.screen.main

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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.Tag
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.main.post_grid.PostGrid
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostToolbarActions
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostView
import ru.xllifi.jetsnatcher.navigation.screen.main.search.Search

@Serializable
data class BrowserNavKey(
  val providerIndex: Int,
  val searchTags: List<Tag>,
) : NavKey

private interface BrowserSubNavKey : NavKey
object BrowserNavigation {
  @Serializable
  object Main : BrowserSubNavKey

  @Serializable
  object Search : BrowserSubNavKey
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Browser(
  providerIndex: Int,
  searchTags: List<Tag>,
  innerPadding: PaddingValues,
  onNewSearch: (providerIndex: Int, searchTags: List<Tag>) -> Unit,
) {
  val viewModel: BrowserViewModel = viewModel(
    factory = BrowserViewModelFactory(
      LocalContext.current,
      providerIndex,
      searchTags
    )
  )

  val backStack = rememberNavBackStack<BrowserSubNavKey>(BrowserNavigation.Main)
  NavDisplay(
    backStack = backStack,
    entryDecorators = listOf(
      rememberSceneSetupNavEntryDecorator(),
      rememberSavedStateNavEntryDecorator(),
      rememberRoundedCornerNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<BrowserNavigation.Main> { key ->
        Main(
          viewModel = viewModel,
          innerPadding = innerPadding,
          postViewToolbarActions = PostToolbarActions(

          ),
          onSearchClick = {
            backStack.add(BrowserNavigation.Search)
          },
        )
      }
      entry<BrowserNavigation.Search> { key ->
        Search(
          providerIndex = providerIndex,
          searchTags = searchTags,
          innerPadding = innerPadding,
          onNewSearch = { providerIndex, searchTags ->
            backStack.remove(BrowserNavigation.Search)
            onNewSearch(providerIndex, searchTags)
          },
        )
      }
    }
  )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Main(
  viewModel: BrowserViewModel,
  innerPadding: PaddingValues,
  postViewToolbarActions: PostToolbarActions,
  onSearchClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  SharedTransitionLayout {
    Column {
      PostGrid(
        modifier = Modifier.weight(1f),
        viewModel = viewModel,
        innerPadding = innerPadding,
        onScrolledToBottom = {
          GlobalScope.launch {
            viewModel.loadPosts()
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