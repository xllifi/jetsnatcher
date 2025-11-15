@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.generic.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.settings.SettingDefaults.settingModifierClickable
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Composable
fun ControlSwitch(
  title: String,
  description: String?,
  checked: Boolean,
  onCheckedChange: (newValue: Boolean) -> Unit,
) {
  Row (
    modifier = Modifier
      .settingModifierClickable(
        role = Role.Switch,
      ) {
        onCheckedChange(!checked)
      },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(
      modifier = Modifier
        .weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = title,
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
      modifier = Modifier
        .heightIn(max = 32.dp),
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(),
      thumbContent = {
        if (checked) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        } else {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        }
      }
    )
  }
}

@Composable
@FullPreview
fun ControlSwitchPreview() {
  var checked by remember { mutableStateOf(true) }
  JetSnatcherTheme {
    ControlSwitch(
      title = "Preview setting",
      description = "Preview setting description.",
      checked,
      onCheckedChange = { checked = it }
    )
  }
}