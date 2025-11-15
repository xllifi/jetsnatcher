@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.generic.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.pxToDp
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntryDefaults
import ru.xllifi.jetsnatcher.ui.settings.SettingDefaults.settingModifier
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Composable
fun <T> ControlDoubleActionList(
  title: String,
  buttonText: String?,
  buttonIcon: ImageVector?,
  onButtonClick: () -> Unit,
  items: List<T>,
  itemTitleTransform: (T) -> String,
  itemDescriptionTransform: (T) -> String,
  itemPrimaryActionIcon: ImageVector?,
  itemSecondaryActionIcon: ImageVector?,
  onItemPrimaryActionClick: ((element: T) -> Unit)?,
  onItemSecondaryActionClick: ((element: T) -> Unit)?,
) = ControlDoubleActionList<T>(
  title = title,
  buttonText = buttonText,
  buttonIcon = buttonIcon,
  onButtonClick = onButtonClick,
  items = items,
  itemTitleTransform = itemTitleTransform,
  itemDescriptionTransform = itemDescriptionTransform,
  itemPrimaryActionIconTransform = { itemPrimaryActionIcon },
  itemSecondaryActionIconTransform = { itemSecondaryActionIcon },
  onItemPrimaryActionClick = onItemPrimaryActionClick,
  onItemSecondaryActionClick = onItemSecondaryActionClick,
)

@Composable
fun <T> ControlDoubleActionList(
  title: String,
  buttonText: String?,
  buttonIcon: ImageVector?,
  onButtonClick: () -> Unit,
  items: List<T>,
  itemTitleTransform: (T) -> String,
  itemDescriptionTransform: (T) -> String?,
  itemPrimaryActionIconTransform: (T) -> ImageVector?,
  itemSecondaryActionIconTransform: (T) -> ImageVector?,
  onItemPrimaryActionClick: ((element: T) -> Unit)?,
  onItemSecondaryActionClick: ((element: T) -> Unit)?,
) {
  Column (
    modifier = Modifier
      .settingModifier(8.dp),
  ) {
    Row(
      modifier = Modifier
        .padding(bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMediumEmphasized.copy(lineHeight = TextUnit.Unspecified),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .padding(8.dp)
          .weight(1f)
      )
      if (buttonText != null && buttonIcon != null) {
        Button(
          shape = CircleShape,
          onClick = onButtonClick,
          modifier = Modifier
            .height(ButtonDefaults.ExtraSmallContainerHeight),
          contentPadding = PaddingValues(12.dp, 0.dp),
        ) {
          Icon(buttonIcon, null)
          Box(Modifier.width(4.dp))
          Text(buttonText)
          Box(Modifier.width(4.dp))
        }
      }
    }
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = LocalWindowInfo.current.containerSize.height.pxToDp())
        .clip(MaterialTheme.shapes.medium),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      items(items) { element ->
        DoubleActionListEntry(
          title = itemTitleTransform(element),
          description = itemDescriptionTransform(element),
          primaryActionIcon = itemPrimaryActionIconTransform(element),
          onPrimaryActionClick = { onItemPrimaryActionClick?.invoke(element) },
          secondaryActionIcon = itemSecondaryActionIconTransform(element),
          onSecondaryActionClick = { onItemSecondaryActionClick?.invoke(element) },
          colors = DoubleActionListEntryDefaults.colors().copy(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
          )
        )
      }
    }
  }
}

@Composable
@FullPreview
fun ControlListPreview() {
  JetSnatcherTheme {
    ControlDoubleActionList(
      title = "Preview setting",
      buttonText = "Add a tag",
      buttonIcon = Icons.Outlined.Add,
      onButtonClick = {},
      items = listOf("Item 1", "Item 2", "Item 3 very very very very very very very very long"),
      itemTitleTransform = { "$it's Title" },
      itemDescriptionTransform = { "$it's Description" },
      itemPrimaryActionIcon = Icons.Outlined.Edit,
      itemSecondaryActionIcon = Icons.Outlined.Delete,
      onItemPrimaryActionClick = {},
      onItemSecondaryActionClick = {},
    )
  }
}