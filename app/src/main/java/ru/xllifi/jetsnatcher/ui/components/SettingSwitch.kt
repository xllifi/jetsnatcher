@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme


@Composable
fun SettingSwitch(
  label: String,
  description: String?,
  checked: Boolean,
  onCheckedChange: (newValue: Boolean) -> Unit,
) {
  Row (
    modifier = Modifier
      .clickable {
        onCheckedChange(!checked)
      }
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .heightIn(min = 64.dp)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(
      modifier = Modifier
        .weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.titleMediumEmphasized.copy(lineHeight = TextUnit.Unspecified),
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (description != null) {
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}

@Composable
@FullPreview
fun SettingSwitchPreview() {
  var checked by remember { mutableStateOf(true) }
  JetSnatcherTheme {
    SettingSwitch(
      label = "Preview setting. Long title to go on a second line PLEASE!",
      description = "Preview setting description. This text is supposed to describe what the option should do. Some more text to make it go on a third line.",
      checked,
      onCheckedChange = { checked = it }
    )
  }
}