package ru.xllifi.jetsnatcher.navigation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.OnInputDialog
import ru.xllifi.jetsnatcher.ui.components.SettingSlider
import ru.xllifi.jetsnatcher.ui.components.SettingSwitch
import ru.xllifi.jetsnatcher.ui.components.SettingTravel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GeneralSettingsPage(
  innerPadding: PaddingValues,
  onManageProviders: () -> Unit,
  onManageBlacklist: () -> Unit,
  onInputDialog: OnInputDialog,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  Column(
    modifier = Modifier.Companion
      .fillMaxSize()
      .padding(innerPadding)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      modifier = Modifier.Companion
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      text = "Settings",
      style = MaterialTheme.typography.titleLargeEmphasized,
      textAlign = TextAlign.Companion.Center,
    )
    val scope = rememberCoroutineScope()
    SettingSlider(
      "Page size", // TODO: translate
      "How many posts to request each time a request is sent", // TODO: translate
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
    SettingSlider(
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
    SettingSwitch(
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
    SettingTravel(
      title = "Manage providers", // TODO: translate
      description = "Open the provider list to edit and delete providers", // TODO: translate
      onClick = onManageProviders
    )
    SettingTravel(
      title = "Manage blacklisted tags", // TODO: translate
      description = "Open the blacklisted tags list to add or remove tags", // TODO: translate
      onClick = onManageBlacklist
    )
  }
}