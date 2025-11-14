package ru.xllifi.jetsnatcher.ui.settings.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settings.BlacklistedTagProto
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settings.SettingsProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.dialog.ProviderEditDialogNavKey
import ru.xllifi.jetsnatcher.ui.settings.SettingsPage
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val defaultProviderType = ProviderType.Gelbooru

@Composable
fun ProvidersSettingsPage(
  onBack: () -> Unit,
  onEditProvider: (navKey: ProviderEditDialogNavKey) -> Unit,
  onDeleteProvider: (
    provider: ProviderProto,
    index: Int,
  ) -> Unit,
  /** only use in [@Previews][androidx.compose.ui.tooling.preview.Preview] */
  previewSettingsProto: State<SettingsProto>? = null,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by previewSettingsProto ?: settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  SettingsPage(
    "Providers",
    onBack = onBack,
  ) {
    group(null) {
      settingDoubleActionList(
        label = "Providers",
        buttonText = "Add provider",
        buttonIcon = Icons.Outlined.Add,
        onButtonClick = {
          onEditProvider(
            ProviderEditDialogNavKey(
              provider = null,
              index = null,
              providerType = defaultProviderType
            )
          )
        },
        items = settingsState.providers.mapIndexed { index, proto -> proto to index },
        itemTitleTransform = {"Edit ${it.first.name}"},
        itemDescriptionTransform = {it.first.routes.base},
        itemPrimaryActionIcon = Icons.Outlined.Edit,
        onItemPrimaryActionClick = {
          onEditProvider(
            ProviderEditDialogNavKey(
              provider = it.first,
              index = it.second,
              providerType = it.first.providerType,
            )
          )
        },
        itemSecondaryActionIcon = Icons.Outlined.DeleteOutline,
        onItemSecondaryActionClick = {
          onDeleteProvider(it.first, it.second)
        },
      )
    }
  }
}

@FullPreviewSysUi
@Composable
private fun ProvidersSettingsPagePreview() {
  JetSnatcherTheme {
    ProvidersSettingsPage(
      onBack = { },
      onEditProvider = { },
      onDeleteProvider = { _, _ -> },
      previewSettingsProto = mutableStateOf(SettingsProto(
        providers = listOf(
          ProviderProto("Gelbooru 1"),
          ProviderProto("Gelbooru 2"),
          ProviderProto("Gelbooru 3"),
        )
      ))
    )
  }
}