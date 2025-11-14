package ru.xllifi.jetsnatcher.ui.settings

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.ui.settings.components.OnInputDialog
import ru.xllifi.jetsnatcher.ui.settings.components.SettingDoubleActionList
import ru.xllifi.jetsnatcher.ui.settings.components.SettingSlider
import ru.xllifi.jetsnatcher.ui.settings.components.SettingSwitch
import ru.xllifi.jetsnatcher.ui.settings.components.SettingTravel
import ru.xllifi.jetsnatcher.ui.settings.components.defaultOnInputDialog

interface SettingsGroupScope {
  fun settingSwitch(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (newValue: Boolean) -> Unit,
  )

  fun settingSlider(
    title: String,
    description: String?,
    value: Float,
    onValueChange: (newValue: Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange steps: Int = 0,
    showDecimal: Boolean = true,
    onInputDialog: OnInputDialog = defaultOnInputDialog,
  )

  fun settingButton(
    title: String,
    description: String?,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
  )

  fun <T> settingDoubleActionList(
    label: String,
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
  )

  fun settingCustom(
    content: @Composable () -> Unit,
  )
}

class SettingsGroupScopeImpl : SettingsGroupScope {
  val items = mutableListOf<@Composable () -> Unit>()

  override fun settingSwitch(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (newValue: Boolean) -> Unit
  ) {
    items.add({
      SettingSwitch(
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange
      )
    })
  }

  override fun settingSlider(
    title: String,
    description: String?,
    value: Float,
    onValueChange: (newValue: Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    showDecimal: Boolean,
    onInputDialog: OnInputDialog
  ) {
    items.add({
      SettingSlider(
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

  override fun settingButton(
    title: String,
    description: String?,
    onClick: () -> Unit,
    trailingIcon: ImageVector?,
  ) {
    items.add({
      SettingTravel(
        title = title,
        description = description,
        onClick = onClick,
        trailingIcon = trailingIcon,
      )
    })
  }
  
  override fun <T> settingDoubleActionList(
    label: String,
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
  ) {
    this.items.add({
      SettingDoubleActionList(
        label = label,
        buttonText = buttonText,
        buttonIcon = buttonIcon,
        onButtonClick = onButtonClick,
        items = items,
        itemTitleTransform = itemTitleTransform,
        itemDescriptionTransform = itemDescriptionTransform,
        itemPrimaryActionIcon = itemPrimaryActionIcon,
        itemSecondaryActionIcon = itemSecondaryActionIcon,
        onItemPrimaryActionClick = onItemPrimaryActionClick,
        onItemSecondaryActionClick = onItemSecondaryActionClick,
      )
    })
  }

  override fun settingCustom(content: @Composable (() -> Unit)) {
    items.add(content)
  }
}

@Composable
fun SettingsGroup(
  title: String,
  content: SettingsGroupScope.() -> Unit,
) {
  SettingsGroup(
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
fun SettingsGroup(
  title: (@Composable (modifier: Modifier) -> Unit)?,
  content: SettingsGroupScope.() -> Unit,
) {
  Column(
    modifier = Modifier
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
    val scope = remember { SettingsGroupScopeImpl() }
    scope.items.clear()
    scope.content()
    Column(
      modifier = Modifier
        .clip(MaterialTheme.shapes.large),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      for (el in scope.items) {
        el()
      }
    }
  }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@FullPreview
private fun SettingsGroupPreview() {
  PreviewSetup {
    Scaffold {
      Box(
        modifier = Modifier
          .padding(16.dp),
      ) {
        var checked by remember { mutableStateOf(false) }
        SettingsGroup(
          title = "Settings group"
        ) {
          settingSwitch(
            title = "Switch",
            description = "Some boolean setting!",
            checked = checked,
            onCheckedChange = { checked = it }
          )
          settingButton(
            title = "Travel setting",
            description = "Takes you to another screen",
            onClick = {}
          )
          settingDoubleActionList(
            label = "Preview setting. Long title to go on a second line and a third, please!!",
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