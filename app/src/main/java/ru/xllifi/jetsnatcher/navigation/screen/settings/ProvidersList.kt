package ru.xllifi.jetsnatcher.navigation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val defaultProviderType = ProviderType.Gelbooru

@Serializable
class ProviderListNavKey : NavKey

@Composable
fun ProviderList(
  innerPadding: PaddingValues,
  onEditProvider: (
    provider: ProviderProto?,
    index: Int?,
    providerType: ProviderType,
  ) -> Unit,
  onDeleteProvider: (
      provider: ProviderProto,
      index: Int,
  ) -> Unit,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  var provider: ProviderProto? by remember { mutableStateOf(null) }
  var providerIndex: Int? by remember { mutableStateOf(null) }
  var showProviderDeleteDialog by remember { mutableStateOf(false) }
  LazyColumn(
    modifier = Modifier
      .padding(16.dp),
    contentPadding = innerPadding,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // TODO: Move to FAB
    item {
      Button(
        onClick = {
          onEditProvider(
            null,
            null,
            defaultProviderType,
          )
        }
      ) {
        Text("New provider")
      }
    }
    itemsIndexed(settingsState.providers) { index, providerFromList ->
      DoubleActionListEntry(
        title = "Edit ${providerFromList.name}",
        description = providerFromList.routes.base,
        primaryActionIcon = Icons.Outlined.Edit,
        onPrimaryAction = {
          onEditProvider(
            providerFromList,
            index,
            providerFromList.providerType,
          )
        },
        secondaryActionIcon = Icons.Outlined.DeleteOutline,
        onSecondaryAction = {
          onDeleteProvider(providerFromList, index)
        },
      )
    }
  }
}

@FullPreviewSysUi
@Composable
fun ProviderListPreview() {
  JetSnatcherTheme {
    ProviderList(PaddingValues(0.dp), { _, _, _ -> }, { _, _ -> })
  }
}