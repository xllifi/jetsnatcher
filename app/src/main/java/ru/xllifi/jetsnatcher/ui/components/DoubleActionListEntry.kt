package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.pxToDp
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DoubleActionListEntry(
  modifier: Modifier = Modifier,
  title: String,
  description: String,
  primaryActionIcon: ImageVector?,
  secondaryActionIcon: ImageVector?,
  onPrimaryActionClick: (() -> Unit)?,
  onSecondaryActionClick: (() -> Unit)?,
  colors: DoubleActionListEntryColors = DoubleActionListEntryDefaults.colors(),
) {
  val density = LocalDensity.current
  var heightDp by remember { mutableStateOf(0.dp) }
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.extraSmall)
      .background(colors.backgroundColor)
      .heightIn(min = 56.dp)
      .onSizeChanged{ heightDp = with(density) { it.height.toDp() } }
    ,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .clickable(onClick = onPrimaryActionClick ?: {}),
      verticalAlignment = Alignment.CenterVertically,
    )
    {
      if (primaryActionIcon != null) {
        Box(
          modifier = Modifier
            .width(56.dp)
            .height(heightDp),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = primaryActionIcon,
            contentDescription = null,
            tint = colors.primaryActionIconColor,
          )
        }
      } else {
        Box(
          modifier = Modifier
            .width(16.dp)
        )
      }
      Column(
        modifier = Modifier
          .padding(vertical = 12.dp)
          .padding(end = 12.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMediumEmphasized.copy(lineHeight = 14.sp),
          color = colors.primaryActionTitleColor,
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 14.sp),
          color = colors.primaryActionDescriptionColor,
        )
      }
    }
    if (onSecondaryActionClick != null && secondaryActionIcon != null) {
      VerticalDivider(
        modifier = Modifier
          .height(heightDp - 24.dp),
        color = colors.dividerColor,
      )
      Box(
        modifier = Modifier
          .clickable { onSecondaryActionClick() }
          .width(56.dp)
          .height(heightDp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = secondaryActionIcon,
          contentDescription = null,
          tint = colors.secondaryActionIconColor,
        )
      }
    }
  }
}

private class IconsPresent : PreviewParameterProvider<Pair<Boolean, Boolean>> {
  override val values: Sequence<Pair<Boolean, Boolean>>
    get() = sequenceOf(
      true to true,
      false to true,
      true to false,
    )
}


@FullPreview
@Composable
private fun DoubleActionListEntryPreview(
  @PreviewParameter(IconsPresent::class) iconsPresent: Pair<Boolean, Boolean>
) {
  JetSnatcherTheme {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
    ) {
      DoubleActionListEntry(
        title = "Preview title",
        description = "Preview description",
        primaryActionIcon = if (iconsPresent.first) Icons.Outlined.TextFields else null,
        onPrimaryActionClick = {},
        secondaryActionIcon = if (iconsPresent.second) Icons.Outlined.TextFields else null,
        onSecondaryActionClick = {},
      )
    }
  }
}

object DoubleActionListEntryDefaults {
  @Composable
  fun colors(): DoubleActionListEntryColors =
    MaterialTheme.colorScheme.defaultDoubleActionListEntryColors

  val ColorScheme.defaultDoubleActionListEntryColors: DoubleActionListEntryColors
    get() {
      return DoubleActionListEntryColors(
        backgroundColor = this.surfaceContainer,
        primaryActionTitleColor = this.primary,
        primaryActionDescriptionColor = this.onBackground,
        primaryActionIconColor = this.primary,
        secondaryActionIconColor = this.primary,
        dividerColor = this.outlineVariant,
      )
    }
}

data class DoubleActionListEntryColors(
  val backgroundColor: Color,
  val primaryActionTitleColor: Color,
  val primaryActionDescriptionColor: Color,
  val primaryActionIconColor: Color,
  val secondaryActionIconColor: Color,
  val dividerColor: Color,
)