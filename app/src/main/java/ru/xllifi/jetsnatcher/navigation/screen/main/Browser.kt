package ru.xllifi.jetsnatcher.navigation.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.Tag
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.main.post_grid.PostGrid
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostToolbarActions
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostView
import ru.xllifi.jetsnatcher.navigation.screen.main.search.Search
import ru.xllifi.jetsnatcher.navigation.screen.settings.SettingsNavKey
import ru.xllifi.jetsnatcher.proto.settingsDataStore

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
  topBackStack: NavBackStack<NavKey>,
  innerPadding: PaddingValues,
) {
  val context = LocalContext.current
  val settings = runBlocking { context.settingsDataStore.data.first() }
  if (settings.providerList.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "No providers found.\nPlease add one in settings.", // TODO: translate
        textAlign = TextAlign.Center,
      )
      Button(
        onClick = {
          topBackStack.add(SettingsNavKey)
        }
      ) {
        Text("Open settings") // TODO: translate
      }
    }
    return
  }

  val viewModel: BrowserViewModel = viewModel(
    factory = BrowserViewModelFactory(
      settings,
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
          topBackStack = topBackStack,
          postViewToolbarActions = PostToolbarActions(
            onDownloadButtonPress = {},
            onCommentButtonPress = {},
            onFavoriteButtonPress = {},
            onInfoButtonPress = { postId -> /* Post details screen*/ },
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
            topBackStack.add(BrowserNavKey(providerIndex, searchTags))
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
  topBackStack: NavBackStack<NavKey>,
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
        onLoadPostsRequest = {
          GlobalScope.launch {
            viewModel.loadPosts()
          }
        },
        onOpenSettings = {
          topBackStack.add(SettingsNavKey)
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
          viewModel = viewModel,
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