package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DoubleActionListEntry(
  title: String,
  description: String,
  primaryActionIcon: ImageVector?,
  secondaryActionIcon: ImageVector?,
  onPrimaryAction: (() -> Unit)?,
  onSecondaryAction: (() -> Unit)?,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .clip(
          MaterialTheme.shapes.medium.copy(
            bottomEnd = CornerSize(4.dp),
            topEnd = CornerSize(4.dp),
          )
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .conditional(
          condition = onPrimaryAction != null,
          ifTrue = Modifier.clickable { onPrimaryAction!!() }
        )
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (primaryActionIcon != null) {
        Icon(
          imageVector = primaryActionIcon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMediumEmphasized.copy(lineHeight = 14.sp),
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 14.sp),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
    if (onSecondaryAction != null && secondaryActionIcon != null) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .clip(
            MaterialTheme.shapes.medium.copy(
              bottomStart = CornerSize(4.dp),
              topStart = CornerSize(4.dp),
            )
          )
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .clickable { onSecondaryAction() }
          .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = secondaryActionIcon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
      }
    }
  }
}

@FullPreview
@Composable
fun DoubleActionListEntryPreview() {
  JetSnatcherTheme {
    DoubleActionListEntry(
      title = "Preview title",
      description = "Preview description",
      primaryActionIcon = Icons.Outlined.TextFields,
      onPrimaryAction = {},
      secondaryActionIcon = Icons.Outlined.ContentCopy,
      onSecondaryAction = {},
    )
  }
}