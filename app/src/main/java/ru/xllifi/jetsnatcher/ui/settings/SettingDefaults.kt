package ru.xllifi.jetsnatcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingDefaults {
  @Composable
  fun Modifier.settingModifier(
    padding: Dp = 16.dp,
  ): Modifier {
    return this.then(
      Modifier
        .clip(MaterialTheme.shapes.extraSmall)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .heightIn(min = 64.dp)
        .padding(padding)
    )
  }
  @Composable
  fun Modifier.settingModifierClickable(
    padding: Dp = 16.dp,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
  ): Modifier {
    return this.then(
      Modifier
        .clip(MaterialTheme.shapes.extraSmall)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .heightIn(min = 64.dp)
        .clickable(
          enabled = enabled,
          onClickLabel = onClickLabel,
          role = role,
          interactionSource = interactionSource,
          onClick = onClick
        )
        .padding(padding)
    )
  }
}