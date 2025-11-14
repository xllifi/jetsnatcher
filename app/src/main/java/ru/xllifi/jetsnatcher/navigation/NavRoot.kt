package ru.xllifi.jetsnatcher.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.xllifi.jetsnatcher.extensions.rememberRoundedCornerNavEntryDecorator
import ru.xllifi.jetsnatcher.navigation.screen.main.Browser
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserNavKey
import ru.xllifi.jetsnatcher.ui.dialog.ProviderEditDialogNavKey
import ru.xllifi.jetsnatcher.navigation.screen.settings.SettingsNavigation
import ru.xllifi.jetsnatcher.navigation.screen.settings.defaultProviderType
import ru.xllifi.jetsnatcher.navigation.screen.settings.settingsNavigation
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.dialog.dialogsNavigation
import java.util.concurrent.CancellationException

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavRoot(
  innerPadding: PaddingValues,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  fun firstProvider() = runBlocking {
    settingsDataStore.data.map { it.providers.firstOrNull() }.firstOrNull()
  }

  val backStack = rememberNavBackStack(BrowserNavKey(firstProvider(), emptyList()))
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
          .padding(horizontal = 16.dp)
          .padding(bottom = innerPadding.calculateBottomPadding() + 8.dp),
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
        Column(
          modifier = Modifier
            .heightIn(max = 256.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Providers",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
          )
          val context = LocalContext.current
          val providerList by context.settingsDataStore.data.map { it.providers }
            .collectAsState(emptyList())
          providerList.forEach { provider ->
            fun isSelected() =
              backStack.last() is BrowserNavKey && (backStack.last() as BrowserNavKey).providerProto == provider
            NavigationDrawerItem(
              icon = { Icon(Icons.Outlined.Image, null) },
              label = { Text(text = provider.name) },
              selected = isSelected(),
              onClick = {
                if (!isSelected()) {
                  val currentSearchTags = if (backStack.last() is BrowserNavKey) {
                    (backStack.last() as BrowserNavKey).searchTags
                  } else {
                    emptyList()
                  }
                  if (backStack.last() is BrowserNavKey && (backStack.last() as BrowserNavKey).providerProto == null) {
                    backStack.removeAt(backStack.lastIndex)
                  }
                  backStack.add(BrowserNavKey(provider, currentSearchTags))
                }
                scope.launch {
                  drawerState.close()
                }
              }
            )
          }
          if (providerList.isEmpty()) {
            Button(
              onClick = {
                backStack.add(ProviderEditDialogNavKey(null, null, defaultProviderType))
              },
              modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
              Text("Add a provider")
            }
          }
        }
        Box(Modifier.weight(1f)) {}
        NavigationDrawerItem(
          icon = { Icon(Icons.Outlined.Settings, null) },
          label = { Text(text = "Settings") },
          selected = backStack.last() is SettingsNavigation.General,
          onClick = {
            if (backStack.last() !is SettingsNavigation.General) {
              backStack.add(SettingsNavigation.General)
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
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberRoundedCornerNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      sceneStrategy = DialogSceneStrategy(),
      entryProvider = entryProvider {
        settingsNavigation(
          innerPadding = innerPadding,
          backStack = backStack,
        )
        entry<BrowserNavKey> { key ->
          Browser(
            providerProto = key.providerProto,
            searchTags = key.searchTags,
            topBackStack = backStack,
            innerPadding = innerPadding,
          )
        }
        dialogsNavigation(
          backStack = backStack
        )
      },
    )
  }
}