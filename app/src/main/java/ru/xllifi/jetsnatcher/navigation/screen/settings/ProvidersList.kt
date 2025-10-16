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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.Provider
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.ConfirmDialog
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val defaultProvider = ProviderType.Gelbooru

@Serializable
class ProviderListNavKey : NavKey

@Composable
fun ProviderList(
  innerPadding: PaddingValues,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  var provider: Provider? by remember { mutableStateOf(null) }
  var providerIndex: Int? by remember { mutableStateOf(null) }
  var providerType by remember { mutableStateOf(defaultProvider) }
  var showProviderInfoDialog by remember { mutableStateOf(false) }
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
          providerType = defaultProvider
          provider = null
          providerIndex = null
          showProviderInfoDialog = true
        }
      ) {
        Text("New provider")
      }
    }
    itemsIndexed(settingsState.providerList) { index, providerFromList ->
      DoubleActionListEntry(
        title = "Edit ${providerFromList.name}",
        description = providerFromList.routes.base,
        primaryActionIcon = Icons.Outlined.Edit,
        onPrimaryAction = {
          providerType = providerFromList.providerType.toReal()
          provider = providerFromList
          providerIndex = index
          showProviderInfoDialog = true
        },
        secondaryActionIcon = Icons.Outlined.DeleteOutline,
        onSecondaryAction = {
          provider = providerFromList
          providerIndex = index
          showProviderDeleteDialog = true
        },
      )
    }
  }

  if (showProviderDeleteDialog) {
    ConfirmDialog(
      title = "Delete ${provider?.name ?: "Unknown provider"}?",
      description = "This action cannot be undone.",
      onDismissRequest = { showProviderDeleteDialog = false },
    ) {
      Button(
        enabled = providerIndex != null,
        onClick = {
          if (providerIndex == null) return@Button
          GlobalScope.launch {
            settingsDataStore.updateData { settings ->
              settings.toBuilder()
                .removeProvider(providerIndex!!)
                .build()
            }
          }
          showProviderDeleteDialog = false
        }
      ) {
        Text("Yes, delete")
      }
      Button(
        onClick = { showProviderDeleteDialog = false }
      ) {
        Text("No, cancel")
      }
    }
  }

  var showProviderTypeDialog by remember { mutableStateOf(false) }
  if (showProviderInfoDialog) {
    ProviderInfoDialog(
      provider = provider,
      onDone = { newProvider ->
        provider = newProvider
        GlobalScope.launch {
          settingsDataStore.updateData { settings ->
            val builder = settings.toBuilder()
            if (providerIndex != null && providerIndex!! >= 0) {
              builder.setProvider(providerIndex!!, provider)
            } else {
              builder.addProvider(provider)
            }
            builder.build()
          }
        }
        showProviderInfoDialog = false
      },
      onDismissRequest = {
        showProviderInfoDialog = false
      },
      providerType = providerType,
      onSelectProviderType = {
        showProviderTypeDialog = true
      }
    )
  }

  if (showProviderTypeDialog) {
    ProviderTypeDialog(
      onDismissRequest = {
        showProviderTypeDialog = false
      },
      onSelectProvider = {
        providerType = it
        showProviderTypeDialog = false
      }
    )
  }
}

@FullPreviewSysUi
@Composable
fun ProviderListPreview() {
  JetSnatcherTheme {
    ProviderList(PaddingValues(0.dp))
  }
}