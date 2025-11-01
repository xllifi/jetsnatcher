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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val defaultProviderType = ProviderType.Gelbooru

@Composable
fun ProvidersSettingsPage(
  innerPadding: PaddingValues,
  onEditProvider: (navKey: ProviderEditDialogNavKey) -> Unit,
  onDeleteProvider: (
    provider: ProviderProto,
    index: Int,
  ) -> Unit,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  LazyColumn(
    modifier = Modifier
      .padding(16.dp),
    contentPadding = innerPadding,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // region TODO: Move to FAB
    item {
      Button(
        onClick = {
          onEditProvider(
            ProviderEditDialogNavKey(
              provider = null,
              index = null,
              providerType = defaultProviderType
            )
          )
        }
      ) {
        Text("New provider")
      }
    }
    // endregion
    itemsIndexed(settingsState.providers) { index, provider ->
      DoubleActionListEntry(
        title = "Edit ${provider.name}",
        description = provider.routes.base,
        primaryActionIcon = Icons.Outlined.Edit,
        onPrimaryAction = {
          onEditProvider(
            ProviderEditDialogNavKey(
              provider = provider,
              index = index,
              providerType = provider.providerType,
            )
          )
        },
        secondaryActionIcon = Icons.Outlined.DeleteOutline,
        onSecondaryAction = {
          onDeleteProvider(provider, index)
        },
      )
    }
  }
}

@FullPreviewSysUi
@Composable
private fun ProvidersSettingsPagePreview() {
  JetSnatcherTheme {
    ProvidersSettingsPage(PaddingValues(0.dp), { }, { _, _ -> })
  }
}