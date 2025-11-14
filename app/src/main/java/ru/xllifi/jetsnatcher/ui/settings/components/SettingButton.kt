@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.settings.SettingDefaults.settingModifierClickable
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme


@Composable
fun SettingTravel(
  title: String,
  description: String?,
  trailingIcon: ImageVector? = null,
  onClick: () -> Unit,
) {
  Row (
    modifier = Modifier
      .settingModifierClickable {
        onClick()
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
    if (trailingIcon != null) {
      Icon(
        imageVector = trailingIcon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
@FullPreview
fun SettingTravelPreview() {
  JetSnatcherTheme {
    SettingTravel(
      title = "Preview setting. Long title to go on a second line PLEASE!",
      description = "Preview setting description. This text is supposed to describe what the option should do. Some more text to make it go on a third line.",
      onClick = {},
      trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
    )
  }
}