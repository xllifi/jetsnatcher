package ru.xllifi.jetsnatcher.ui.generic

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlButton
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlDoubleActionList
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlSlider
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlSwitch
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlTextField
import ru.xllifi.jetsnatcher.ui.generic.controls.OnInputDialog
import ru.xllifi.jetsnatcher.ui.generic.controls.defaultOnInputDialog

class ControlsGroupScope {
  val items = mutableListOf<@Composable () -> Unit>()

  fun controlSwitch(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (newValue: Boolean) -> Unit
  ) {
    items.add({
      ControlSwitch(
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange
      )
    })
  }

  fun controlSlider(
    title: String,
    description: String?,
    value: Float,
    onValueChange: (newValue: Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange steps: Int = 0,
    showDecimal: Boolean = true,
    onInputDialog: OnInputDialog = defaultOnInputDialog,
  ) {
    items.add({
      ControlSlider(
        title = title,
        description = description,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        showDecimal = showDecimal,
        onInputDialog = onInputDialog,
      )
    })
  }

  fun controlButton(
    title: String,
    description: String?,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
  ) {
    items.add({
      ControlButton(
        title = title,
        description = description,
        onClick = onClick,
        trailingIcon = trailingIcon,
      )
    })
  }
  fun <T> controlDoubleActionList(
    title: String,
    buttonText: String?,
    buttonIcon: ImageVector?,
    onButtonClick: () -> Unit,
    items: List<T>,
    itemTitleTransform: (T) -> String,
    itemDescriptionTransform: (T) -> String?,
    itemPrimaryActionIcon: ImageVector?,
    itemSecondaryActionIcon: ImageVector?,
    onItemPrimaryActionClick: ((element: T) -> Unit)?,
    onItemSecondaryActionClick: ((element: T) -> Unit)?,
  ) = controlDoubleActionList<T>(
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

  fun <T> controlDoubleActionList(
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
    this.items.add({
      ControlDoubleActionList(
        title = title,
        buttonText = buttonText,
        buttonIcon = buttonIcon,
        onButtonClick = onButtonClick,
        items = items,
        itemTitleTransform = itemTitleTransform,
        itemDescriptionTransform = itemDescriptionTransform,
        itemPrimaryActionIconTransform = itemPrimaryActionIconTransform,
        itemSecondaryActionIconTransform = itemSecondaryActionIconTransform,
        onItemPrimaryActionClick = onItemPrimaryActionClick,
        onItemSecondaryActionClick = onItemSecondaryActionClick,
      )
    })
  }

  fun controlTextField(
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
    this.items.add({
      ControlTextField(
        title = title,
        description = description,
        value = value,
        onValueChange = onValueChange,
        onKeyboardDone = onKeyboardDone,
        icon = icon,
        placeholder = placeholder,
        label = label,
        acceptableCharactersRegex = acceptableCharactersRegex,
        singleLine = singleLine,
      )
    })
  }

  fun controlCustom(content: @Composable (() -> Unit)) {
    items.add(content)
  }
}

@Composable
fun ControlsGroup(
  title: String,
  content: ControlsGroupScope.() -> Unit,
) {
  ControlsGroup(
    title = {
      Text(
        modifier = it,
        text = title
      )
    },
    content = content
  )
}

@Composable
fun ControlsGroup(
  modifier: Modifier = Modifier,
  title: (@Composable (modifier: Modifier) -> Unit)?,
  content: ControlsGroupScope.() -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth(),
  ) {
    if (title != null) {
      val titleTextStyle = MaterialTheme.typography.titleMedium.copy(
        color = MaterialTheme.colorScheme.primary
      )
      CompositionLocalProvider(
        LocalTextStyle provides titleTextStyle
      ) {
        title(
          Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .padding(horizontal = 8.dp)
        )
      }
    }
    val scope = remember { ControlsGroupScope() }
    scope.items.clear()
    scope.content()
    Column(
      modifier = Modifier
        .clip(MaterialTheme.shapes.large),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      scope.items.forEach { it() }
    }
  }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@FullPreview
private fun ControlsGroupPreview() {
  PreviewSetup {
    Scaffold {
      Box(
        modifier = Modifier
          .padding(16.dp),
      ) {
        ControlsGroup(
          title = "Controls group"
        ) {
          controlTextField(
            title = "Text field",
            description = "Something to enter text",
            value = "InitVal",
            onValueChange = { },
            icon = Icons.Default.TextFields,
          )
          controlSwitch(
            title = "Switch",
            description = "Some boolean control!",
            checked = false,
            onCheckedChange = { }
          )
          controlButton(
            title = "Travel control",
            description = "Takes you to another screen",
            onClick = {},
            trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
          )
          controlDoubleActionList(
            title = "Preview control. Long title to go on a second line and a third, please!!",
            buttonText = "Add a tag",
            buttonIcon = Icons.Outlined.Add,
            onButtonClick = {},
            items = listOf("Item 1", "Item 2", "Item 3"),
            itemTitleTransform = { "$it's Title" },
            itemDescriptionTransform = { "$it's Description" },
            itemPrimaryActionIcon = Icons.Outlined.Edit,
            itemSecondaryActionIcon = Icons.Outlined.Delete,
            onItemPrimaryActionClick = {},
            onItemSecondaryActionClick = {},
          )
        }
      }
    }
  }
}