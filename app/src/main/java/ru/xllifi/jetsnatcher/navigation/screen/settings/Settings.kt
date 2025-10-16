@file:OptIn(ExperimentalMaterial3Api::class)

package ru.xllifi.jetsnatcher.navigation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.SettingSlider
import ru.xllifi.jetsnatcher.ui.components.SettingSwitch

@Serializable
class SettingsNavKey : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Settings(
  innerPadding: PaddingValues,
  onEditProviders: () -> Unit,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      text = "Settings",
      style = MaterialTheme.typography.titleLargeEmphasized,
      textAlign = TextAlign.Center,
    )
    val scope = rememberCoroutineScope()
    SettingSlider(
      "Page size",
      "How many posts to request each time a request is sent.",
      value = settingsState.pageSize.toFloat(),
      onValueChange = { value ->
        scope.launch {
          settingsDataStore.updateData { currentSettings ->
            currentSettings
              .toBuilder()
              .setPageSize(value.toInt())
              .build()
          }
        }
      },
      valueRange = 10f..100f,
      steps = 8,
      showDecimal = false,
    )
    SettingSlider(
      "Double tap threshold",
      "How fast you should double-tap the screen for it to register as double tap",
      value = settingsState.doubleTapThreshold.toFloat(),
      onValueChange = { value ->
        scope.launch {
          settingsDataStore.updateData { currentSettings ->
            currentSettings
              .toBuilder()
              .setDoubleTapThreshold(value.toInt())
              .build()
          }
        }
      },
      valueRange = 100f..500f,
      steps = 7,
      showDecimal = false,
    )
    SettingSwitch(
      label = "Show post info in grid view",
      description = "Whether to show some general post info below each card in post view",
      checked = settingsState.showCardInfo,
      onCheckedChange = { value ->
        scope.launch {
          settingsDataStore.updateData { currentSettings ->
            currentSettings
              .toBuilder()
              .setShowCardInfo(value)
              .build()
          }
        }
      }
    )
    Button(
      onClick = onEditProviders
    ) {
      Text("Edit providers")
    }
    // tODO: setting button
  }
}