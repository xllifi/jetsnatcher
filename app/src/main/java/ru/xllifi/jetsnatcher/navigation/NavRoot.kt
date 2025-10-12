package ru.xllifi.jetsnatcher.navigation

import ru.xllifi.jetsnatcher.navigation.screen.browser.PostToolbarActions
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.settings.SettingsNavKey
import ru.xllifi.jetsnatcher.navigation.screen.browser.Browser
import ru.xllifi.jetsnatcher.navigation.screen.browser.BrowserNavKey
import ru.xllifi.jetsnatcher.navigation.screen.browser.BrowserViewModel
import ru.xllifi.jetsnatcher.navigation.screen.post_details.PostDetails
import ru.xllifi.jetsnatcher.navigation.screen.post_details.PostDetailsNavKey
import ru.xllifi.jetsnatcher.navigation.screen.search.Search
import ru.xllifi.jetsnatcher.navigation.screen.search.SearchNavKey
import ru.xllifi.jetsnatcher.navigation.screen.settings.Settings
import ru.xllifi.jetsnatcher.navigation.screen.settings.SettingsViewModel

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavRoot(
  innerPadding: PaddingValues,
) {
  val backStack = rememberNavBackStack(BrowserNavKey())
  SharedTransitionLayout {
    NavDisplay(
      backStack = backStack,
      entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        rememberRoundedCornerNavEntryDecorator(),
      ),
      entryProvider = entryProvider {
        entry<BrowserNavKey> { key ->
          val viewModel: BrowserViewModel = viewModel()
          Browser(
            viewModel = viewModel,
            innerPadding = innerPadding,
            postViewToolbarActions = PostToolbarActions(
              onInfoButtonPress = { id ->
                backStack.add(PostDetailsNavKey(id))
              }
            ),
            onSearchClick = {
              backStack.add(SearchNavKey)
            }
          )
        }
        entry<SettingsNavKey> { key ->
          val viewModel: SettingsViewModel = viewModel()
          Settings(viewModel, innerPadding)
        }
        entry<PostDetailsNavKey> { key ->
          PostDetails(
            postId = key.postId,
            innerPadding = innerPadding,
          )
        }
        entry<SearchNavKey> {
          val viewModel: BrowserViewModel = viewModel()
          Search(
            browserViewModel = viewModel,
            innerPadding = innerPadding,
            onNewSearch = { backStack.remove(SearchNavKey) }
          )
        }
      },
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)
      .padding(horizontal = 12.dp, vertical = 128.dp)
  ) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    FloatingActionButtonMenu (
      modifier = Modifier.align(Alignment.BottomEnd),
      expanded = fabMenuExpanded,
      button = {
        ToggleFloatingActionButton(
          checked = fabMenuExpanded,
          onCheckedChange = {
            fabMenuExpanded = !fabMenuExpanded
          }
        ) {
          val imageVector by remember {
            derivedStateOf {
              if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
            }
          }
          Icon(
            painter = rememberVectorPainter(imageVector),
            contentDescription = null,
            modifier = Modifier.animateIcon({ checkedProgress }),
          )
        }
      },
    ) {
      FloatingActionButtonMenuItem(
        onClick = {
          fabMenuExpanded = false
          backStack.add(BrowserNavKey())
        },
        text = {
          Text("Browser")
        },
        icon = {
          Icon(Icons.Outlined.Image, null)
        },
      )
      FloatingActionButtonMenuItem(
        onClick = {
          fabMenuExpanded = false
          backStack.add(SettingsNavKey())
        },
        text = {
          Text("Settings")
        },
        icon = {
          Icon(Icons.Outlined.Settings, null)
        },
      )
    }
  }
}