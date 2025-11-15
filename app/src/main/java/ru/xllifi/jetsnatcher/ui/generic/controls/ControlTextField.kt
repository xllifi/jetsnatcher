@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.generic.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.components.TextField
import ru.xllifi.jetsnatcher.ui.components.TextFieldDefaults
import ru.xllifi.jetsnatcher.ui.settings.SettingDefaults.settingModifier
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Composable
fun ControlTextField(
  title: String,
  description: String?,
  value: String,
  onValueChange: (String) -> Unit,
  onKeyboardDone: (String) -> Unit = onValueChange,
  icon: ImageVector? = null,
  placeholder: String? = null,
  label: String? = null,
  acceptableCharactersRegex: Regex? = null,
  singleLine: Boolean = false,
) {
  Column (
    modifier = Modifier.settingModifier(8.dp),
    verticalArrangement = Arrangement.Center,
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
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
    TextField(
      value = value,
      onValueChange = onValueChange,
      onKeyboardDone = onKeyboardDone,
      icon = icon,
      placeholder = placeholder,
      label = label,
      acceptableCharactersRegex = acceptableCharactersRegex,
      singleLine = singleLine,
      colors = TextFieldDefaults.colors().copy(
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
      )
    )

  }
}

@Composable
@FullPreview
fun ControlTextFieldPreview() {
  JetSnatcherTheme {
    ControlTextField(
      title = "Preview setting. Long title to go on a second line PLEASE!",
      description = "Preview setting description. This text is supposed to describe what the option should do. Some more text to make it go on a third line.",
      value = "InitVal",
      onValueChange = { },
      icon = Icons.Default.TextFields,
    )
  }
}