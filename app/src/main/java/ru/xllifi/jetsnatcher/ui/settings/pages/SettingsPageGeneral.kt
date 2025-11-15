package ru.xllifi.jetsnatcher.ui.settings.pages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.generic.controls.OnInputDialog
import ru.xllifi.jetsnatcher.ui.settings.SettingsPage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GeneralSettingsPage(
  onBack: () -> Unit,
  onManageProviders: () -> Unit,
  onManageBlacklist: () -> Unit,
  onInputDialog: OnInputDialog,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)
  val scope = rememberCoroutineScope()

  SettingsPage(
    title = "Settings",
    onBack = onBack,
  ) {
    controlsGroup("Post list") {
      controlSlider(
        title = "Posts per content load",
        description = "How many posts to request from a provider. Actual amount of posts may be lower depending on provider's limits.",
        value = settingsState.pageSize.toFloat(),
        onValueChange = { value ->
          scope.launch {
            settingsDataStore.updateData {
              it.copy(
                pageSize = value.toUInt()
              )
            }
          }
        },
        valueRange = 10f..100f,
        steps = 8,
        showDecimal = false,
        onInputDialog = onInputDialog,
      )
      controlSwitch(
        title = "Show post info in grid view", // TODO: translate
        description = "Whether to show some general post info below each card in post view", // TODO: translate
        checked = settingsState.showCardInfo,
        onCheckedChange = { value ->
          scope.launch {
            settingsDataStore.updateData {
              it.copy(
                showCardInfo = value
              )
            }
          }
        }
      )
    }
    controlsGroup("Post view") {
      controlSlider(
        "Double tap threshold", // TODO: translate
        "How fast you should double-tap the screen for it to register as double tap", // TODO: translate
        value = settingsState.doubleTapThreshold.toFloat(),
        onValueChange = { value ->
          scope.launch {
            settingsDataStore.updateData {
              it.copy(
                doubleTapThreshold = value.toUInt()
              )
            }
          }
        },
        valueRange = 100f..500f,
        steps = 7,
        showDecimal = false,
        onInputDialog = onInputDialog,
      )
    }
    controlsGroup("Miscellaneous") {
      controlButton(
        title = "Manage providers", // TODO: translate
        description = "Open the provider list to edit and delete providers", // TODO: translate
        onClick = onManageProviders,
        trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
      )
      controlButton(
        title = "Manage blacklisted tags", // TODO: translate
        description = "Open the blacklisted tags list to add or remove tags", // TODO: translate
        onClick = onManageBlacklist,
        trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
      )
    }
  }
}

@Composable
@FullPreview
fun GeneralSettingsPagePreview() {
  PreviewSetup {
    GeneralSettingsPage(
      onBack = { },
      onManageProviders = { },
      onManageBlacklist = { },
      onInputDialog = { },
    )
  }
}