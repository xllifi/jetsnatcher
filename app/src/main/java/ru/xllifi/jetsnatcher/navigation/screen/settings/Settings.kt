@file:OptIn(ExperimentalMaterial3Api::class)

package ru.xllifi.jetsnatcher.navigation.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserNavKey
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.dialog.ConfirmDialogNavKey

object SettingsNavigation {
  interface SettingsNavKey : NavKey

  @Serializable
  object General : SettingsNavKey

  @Serializable
  object Providers : SettingsNavKey

  @Serializable
  object Blacklist : SettingsNavKey
}

fun EntryProviderScope<NavKey>.settingsNavigation(
  innerPadding: PaddingValues,
  backStack: NavBackStack<NavKey>,
) {
  entry<SettingsNavigation.General> {
    GeneralSettingsPage(
      innerPadding = innerPadding,
      onManageProviders = {
        backStack.add(SettingsNavigation.Providers)
      },
      onManageBlacklist = {
        backStack.add(SettingsNavigation.Blacklist)
      },
      onInputDialog = { navKey ->
        backStack.add(navKey)
      },
    )
  }
  entry<SettingsNavigation.Providers> {
    val settingsDataStore = LocalContext.current.settingsDataStore
    ProvidersSettingsPage(
      innerPadding = innerPadding,
      onEditProvider = { navKey ->
        backStack.add(navKey)
      },
      onDeleteProvider = { provider, index ->
        backStack.add(
          ConfirmDialogNavKey(
            title = "Delete ${provider.name}?",
            description = "This action cannot be undone.",
          ) { onDismiss ->
            Button(
              onClick = {
                backStack.removeAll {
                  it is BrowserNavKey && it.providerProto == provider
                }
                GlobalScope.launch {
                  settingsDataStore.updateData { settings ->
                    val providers = settings.providers.toMutableList()
                    providers.removeAt(index)
                    settings.copy(
                      providers = providers
                    )
                  }
                }
                if (backStack.first() !is BrowserNavKey) {
                  backStack.add(0, BrowserNavKey(null, emptyList()))
                }
                onDismiss()
              }
            ) {
              Text("Yes, delete")
            }
            Button(
              onClick = onDismiss
            ) {
              Text("No, cancel")
            }
          })
      },
    )
  }
  entry<SettingsNavigation.Blacklist> {
    BlacklistSettingsPage(
      innerPadding = innerPadding,
      onAddTag = { navKey -> backStack.add(navKey) },
      onRemoveTag = { navKey -> backStack.add(navKey) },
    )
  }
}

