package ru.xllifi.jetsnatcher.ui.settings.pages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.xllifi.jetsnatcher.extensions.FullPreviewSysUi
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.extensions.timestampToRelativeTimeSpan
import ru.xllifi.jetsnatcher.proto.SettingsSerializer
import ru.xllifi.jetsnatcher.proto.settings.BlacklistedTagProto
import ru.xllifi.jetsnatcher.proto.settings.SettingsProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.dialog.ConfirmDialogNavKey
import ru.xllifi.jetsnatcher.ui.dialog.TextFieldDialogNavKey
import ru.xllifi.jetsnatcher.ui.settings.SettingsPage

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlacklistSettingsPage(
  onAddTag: (navKey: TextFieldDialogNavKey) -> Unit,
  onRemoveTag: (navKey: ConfirmDialogNavKey) -> Unit,
  /** only use in [@Previews][androidx.compose.ui.tooling.preview.Preview] */
  previewSettingsProto: State<SettingsProto>? = null,
) {
  val settingsDataStore = LocalContext.current.settingsDataStore
  val settingsState by previewSettingsProto ?: settingsDataStore.data.collectAsState(SettingsSerializer.defaultValue)

  SettingsPage(
    title = "Blacklist",
    onBack = {}
  ) {
    group(null) {
      settingDoubleActionList(
        label = "Blacklisted tags",
        buttonText = "Add tag",
        buttonIcon = Icons.Outlined.Add,
        onButtonClick = {
          onAddTag(
            TextFieldDialogNavKey(
              title = "Add blacklisted tag",
              description = "Input a tag value",
              initValue = "",
              onDone = { value -> addBlacklistedTag(settingsDataStore, value) },
            )
          )
        },
        items = settingsState.blacklistedTags,
        itemTitleTransform = { it.value },
        itemDescriptionTransform = { "Added ${timestampToRelativeTimeSpan(it.createdAt)}" },
        itemPrimaryActionIcon = Icons.Outlined.Tag,
        onItemPrimaryActionClick = null,
        itemSecondaryActionIcon = Icons.Outlined.Delete,
        onItemSecondaryActionClick = {
          onRemoveTag(
            ConfirmDialogNavKey(
              title = "Remove ${it.value} from blacklist?",
              description = "This action cannot be undone.",
            ) { onDismiss ->
              Button(
                onClick = {
                  removeBlacklistedTag(settingsDataStore, it.value)
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
        }
      )
    }
  }
}

val previewBlacklistedTags = listOf(
  BlacklistedTagProto(0, "tag1"),
  BlacklistedTagProto(1763140545322, "tag2"),
  BlacklistedTagProto(0, "tag3"),
)

@Composable
@FullPreviewSysUi
private fun BlacklistSettingsPagePreview() {
  PreviewSetup {
    BlacklistSettingsPage(
      onAddTag = { },
      onRemoveTag = { },
      previewSettingsProto = mutableStateOf(SettingsProto(
        blacklistedTags = listOf(
          BlacklistedTagProto(0, "tag1"),
          BlacklistedTagProto(0, "tag2"),
          BlacklistedTagProto(0, "tag3"),
          BlacklistedTagProto(1763147868000, "tag4"),
          BlacklistedTagProto(0, "very_very_very_very_very_very_very_very_very_very_very_very_long_tag"),
        )
      ))
    )
  }
}