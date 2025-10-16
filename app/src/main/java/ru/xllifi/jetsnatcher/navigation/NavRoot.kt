package ru.xllifi.jetsnatcher.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.main.Browser
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserNavKey
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModelFactory
import ru.xllifi.jetsnatcher.navigation.screen.settings.ProviderList
import ru.xllifi.jetsnatcher.navigation.screen.settings.ProviderListNavKey
import ru.xllifi.jetsnatcher.navigation.screen.settings.Settings
import ru.xllifi.jetsnatcher.navigation.screen.settings.SettingsNavKey
import java.util.concurrent.CancellationException

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavRoot(
  innerPadding: PaddingValues,
) {
  val backStack = rememberNavBackStack(BrowserNavKey(0, emptyList()))
  val drawerState = rememberDrawerState(
    initialValue = DrawerValue.Closed
  )
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      var predictiveBackProgress by remember { mutableStateOf(0f) }
      val scale by remember { derivedStateOf { 1f - (predictiveBackProgress * 0.1f) } }
      val offsetX by remember { derivedStateOf { 24.dp * -predictiveBackProgress } }
      PredictiveBackHandler(drawerState.isOpen) { progress ->
        try {
          progress.collect { backEvent ->
            predictiveBackProgress = backEvent.progress
            // Handle gesture progress updates here.
          }
          // This block is executed if the gesture completes successfully.
          drawerState.close()
        } catch (e: CancellationException) {
          // This block is executed if the gesture is cancelled.
          predictiveBackProgress = 0f
        } finally {
          predictiveBackProgress = 0f
          // This block is executed either the gesture is completed or cancelled.
        }
      }
      Column(
        modifier = Modifier
          .fillMaxSize()
          .offset(
            x = offsetX,
            y = 0.dp,
          )
          .scale(scale)
          .padding(end = 72.dp)
          .clip(MaterialTheme.shapes.extraLarge)
          .shadow(16.dp, shape = MaterialTheme.shapes.extraLarge)
          .background(MaterialTheme.colorScheme.background)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        val density = LocalDensity.current
        val topPadding = innerPadding.calculateTopPadding()
        Text(
          text = "jetSnatcher",
          style = MaterialTheme.typography.titleLargeEmphasized,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .clip(
              MaterialTheme.shapes.extraLarge.copy(
                topEnd = ZeroCornerSize,
                topStart = ZeroCornerSize,
              )
            )
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
              drawContent()
              val heightPx = with(density) { topPadding.toPx() }
              drawRect(
                size = Size(
                  width = this.size.width,
                  height = heightPx,
                ),
                brush = Brush.verticalGradient(
                  0.0f to Color.Black,
                  1.0f to Color.Transparent,
                  startY = heightPx / 3,
                  endY = heightPx,
                ),
                blendMode = BlendMode.DstOut,
              )
            }
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = topPadding)
            .padding(20.dp, 16.dp)
            .fillMaxWidth()
        )
        val scope = rememberCoroutineScope()
        NavigationDrawerItem(
          icon = { Icon(Icons.Outlined.Image, null) },
          label = { Text(text = "Browser") },
          selected = backStack.last()::class == BrowserNavKey::class,
          onClick = {
            if (backStack.last()::class != BrowserNavKey::class) {
              backStack.add(BrowserNavKey(0, emptyList()))
              scope.launch {
                drawerState.close()
              }
            }
          }
        )
        NavigationDrawerItem(
          icon = { Icon(Icons.Outlined.Settings, null) },
          label = { Text(text = "Settings") },
          selected = backStack.last()::class == SettingsNavKey::class,
          onClick = {
            if (backStack.last()::class != SettingsNavKey::class) {
              backStack.add(SettingsNavKey)
              scope.launch {
                drawerState.close()
              }
            }
          }
        )
      }
    }
  ) {
    NavDisplay(
      backStack = backStack,
      entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        rememberRoundedCornerNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = entryProvider {
        entry<BrowserNavKey> { key ->
          Browser(
            providerIndex = key.providerIndex,
            searchTags = key.searchTags,
            topBackStack = backStack,
            innerPadding = innerPadding,
          )
        }
        entry<SettingsNavKey> { key ->
          Settings(
            innerPadding = innerPadding,
            onEditProviders = {
              backStack.add(ProviderListNavKey())
            }
          )
        }
        entry<ProviderListNavKey> { key ->
          ProviderList(
            innerPadding = innerPadding,
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
    FloatingActionButtonMenu(
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
          backStack.add(
            BrowserNavKey(
              0, listOf(
                Tag(
                  label = "tag1",
                  value = "tag1",
                  postCount = 0,
                  category = TagCategory.General,
                )
              )
            )
          )
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
          backStack.add(SettingsNavKey)
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