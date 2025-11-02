package ru.xllifi.jetsnatcher.navigation.screen.settings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.datastore.core.DataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.extensions.timestampToRelativeTimeSpan
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settings.BlacklistedTagProto
import ru.xllifi.jetsnatcher.proto.settings.SettingsProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.ConfirmDialogNavKey
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.components.TextFieldDialogNavKey
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

fun addBlacklistedTag(settingsDataStore: DataStore<SettingsProto>, vararg values: String) {
  GlobalScope.launch {
    settingsDataStore.updateData {
      val blacklistedTags = it.blacklistedTags.toMutableList()
      blacklistedTags.addAll(
        values
          .filter { value -> value.isNotEmpty() && !blacklistedTags.any { tag -> tag.value == value } }
          .map { value ->
            BlacklistedTagProto(
              createdAt = System.currentTimeMillis(),
              value = value
            )
          }
      )
      it.copy(
        blacklistedTags = blacklistedTags
      )
    }
  }
}

fun removeBlacklistedTag(settingsDataStore: DataStore<SettingsProto>, vararg values: String) {
  GlobalScope.launch {
    settingsDataStore.updateData {
      val blacklistedTags = it.blacklistedTags.toMutableList()
      blacklistedTags.removeAll { tag -> values.contains(tag.value) }
      it.copy(
        blacklistedTags = blacklistedTags
      )
    }
  }
}

@Composable
fun BlacklistSettingsPage(
  innerPadding: PaddingValues,
  onAddTag: (navKey: TextFieldDialogNavKey) -> Unit,
  onRemoveTag: (navKey: ConfirmDialogNavKey) -> Unit,
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
          onAddTag(
            TextFieldDialogNavKey(
              title = "Add blacklisted tag",
              description = "Input a tag value",
              initValue = "",
              onDone = { value -> addBlacklistedTag(settingsDataStore, value) },
            )
          )
        }
      ) {
        Text("Add tag")
      }
    }
    // endregion
    itemsIndexed(settingsState.blacklistedTags) { index, tag ->
      DoubleActionListEntry(
        title = tag.value,
        description = "Added ${timestampToRelativeTimeSpan(tag.createdAt)}",
        primaryActionIcon = null,
        onPrimaryAction = null,
        secondaryActionIcon = Icons.Outlined.Delete,
        onSecondaryAction = {
          onRemoveTag(
            ConfirmDialogNavKey(
              title = "Remove ${tag.value} from blacklist?",
              description = "This action cannot be undone.",
            ) { onDismiss ->
              Button(
                onClick = {
                  removeBlacklistedTag(settingsDataStore, tag.value)
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
            }
          )
        },
      )
    }
  }
}

@FullPreviewSysUi
@Composable
private fun BlacklistSettingsPagePreview() {
  JetSnatcherTheme {
    BlacklistSettingsPage(PaddingValues(0.dp), { }, { })
  }
}