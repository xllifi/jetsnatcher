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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.Tag
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.main.post_details.PostDetails
import ru.xllifi.jetsnatcher.navigation.screen.main.post_grid.PostGrid
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostToolbarActions
import ru.xllifi.jetsnatcher.navigation.screen.main.post_view.PostView
import ru.xllifi.jetsnatcher.navigation.screen.main.search.Search
import ru.xllifi.jetsnatcher.ui.dialog.ProviderEditDialogNavKey
import ru.xllifi.jetsnatcher.ui.settings.pages.addBlacklistedTag
import ru.xllifi.jetsnatcher.proto.history.HistoryEntryProto
import ru.xllifi.jetsnatcher.proto.historyDataStore
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore

@Serializable
data class BrowserNavKey(
  val providerProto: ProviderProto?,
  val searchTags: List<Tag>,
) : NavKey

private interface BrowserSubNavKey : NavKey
object BrowserNavigation {
  @Serializable
  object Main : BrowserSubNavKey

  @Serializable
  data class PostDetails(val postIndex: Int) : BrowserSubNavKey

  @Serializable
  object Search : BrowserSubNavKey
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Browser(
  providerProto: ProviderProto?,
  searchTags: List<Tag>,
  topBackStack: NavBackStack<NavKey>,
  innerPadding: PaddingValues,
) {
  val context = LocalContext.current
  val providers by context.settingsDataStore.data.map { it.providers }.collectAsState(emptyList())
  if (providers.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "No providers found.\nPlease add one in settings or with a button below.", // TODO: translate
        textAlign = TextAlign.Center,
      )
      Button(
        onClick = {
          topBackStack.add(ProviderEditDialogNavKey(providerType = ProviderType.Gelbooru))
        }
      ) {
        Text("Add a provider") // TODO: translate
      }
    }
    return
  }
  if (providerProto == null) {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "No provider selected.\nPlease select one in navigation drawer (swipe left-to-right)", // TODO: translate
        textAlign = TextAlign.Center,
      )
    }
    return
  }

  val viewModel: BrowserViewModel = viewModel(
    factory = BrowserViewModelFactory(
      context,
      providerProto,
      searchTags
    )
  )

  val scope = rememberCoroutineScope()
  val localBackStack = rememberNavBackStack(BrowserNavigation.Main)
  NavDisplay(
    backStack = localBackStack,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberRoundedCornerNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<BrowserNavigation.Main> { key ->
        Main(
          viewModel = viewModel,
          innerPadding = innerPadding,
          postViewToolbarActions = PostToolbarActions(
            onDownloadButtonPress = {},
            onCommentButtonPress = {},
            onFavoriteButtonPress = {},
            onInfoButtonPress = { postIndex ->
              localBackStack.add(BrowserNavigation.PostDetails(postIndex))
            },
          ),
          onSearchClick = {
            localBackStack.add(BrowserNavigation.Search)
          },
          onEditProviderClick = {
            topBackStack.add(
              ProviderEditDialogNavKey(
                provider = providerProto,
                index = providers.indexOf(providerProto),
                providerType = providerProto.providerType
              )
            )
          }
        )
      }
      fun newSearch(tags: List<Tag>) {
        topBackStack.add(BrowserNavKey(providerProto, tags))
        scope.launch {
          context.historyDataStore.updateData { history ->
            val entries = history.entries.toMutableList()
            val isFavorite = entries.firstOrNull { it.tags == tags }?.isFavorite ?: false
            entries.removeAll { it.tags == tags }
            entries.add(
              HistoryEntryProto(
                createdAt = System.currentTimeMillis(),
                tags = tags,
                isFavorite = isFavorite,
              )
            )
            history.copy(
              entries = entries
            )
          }
        }
      }
      entry<BrowserNavigation.Search> { key ->
        Search(
          providerProto = providerProto,
          searchTags = searchTags,
          innerPadding = innerPadding,
          onNewSearch = { providerProto, tags ->
            localBackStack.remove(BrowserNavigation.Search)
            newSearch(tags)
          },
        )
      }
      entry<BrowserNavigation.PostDetails> { key ->
        PostDetails(
          browserViewModel = viewModel,
          postIndex = key.postIndex,
          innerPadding = innerPadding,
          onSelectedTagsAddToSearchClick = { tags -> newSearch(searchTags + tags) },
          onSelectedTagsNewSearchClick = { tags -> newSearch(tags) },
          onSelectedTagsAddToBlacklistClick = { tags ->
            addBlacklistedTag(context.settingsDataStore, *tags.map { it.value }.toTypedArray())
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
  onEditProviderClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(uiState.selectedPostIndex) {
    if (uiState.expandPost && uiState.selectedPostIndex > uiState.posts.lastIndex - 4) {
      GlobalScope.launch {
        viewModel.loadPosts()
      }
    }
  }
  SharedTransitionLayout {
    Column {
      PostGrid(
        modifier = Modifier.weight(1f),
        viewModel = viewModel,
        innerPadding = innerPadding,
        onLoadPostsRequest = { evenIfNoMore ->
          GlobalScope.launch {
            viewModel.loadPosts(evenIfNoMore)
          }
        },
        onEditProviderClick
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
        viewModel = viewModel,
        animatedVisibilityScope = this,
        onBack = { viewModel.expandPost(to = false) },
        onSelectedPostChange = { viewModel.selectPost(it) },
        innerPadding = innerPadding,
        postToolbarActions = postViewToolbarActions,
      )
    }
  }
}