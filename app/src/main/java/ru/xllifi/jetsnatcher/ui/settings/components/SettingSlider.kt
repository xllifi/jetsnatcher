@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.xllifi.jetsnatcher.ui.settings.components

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.numPlaces
import ru.xllifi.jetsnatcher.ui.settings.SettingDefaults.settingModifier
import ru.xllifi.jetsnatcher.ui.dialog.TextFieldDialogNavKey
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

typealias OnInputDialog = (navKey: TextFieldDialogNavKey) -> Unit
val defaultOnInputDialog: OnInputDialog = { }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingSlider(
  title: String,
  description: String?,
  value: Float,
  onValueChange: (newValue: Float) -> Unit,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  @IntRange steps: Int = 0,
  showDecimal: Boolean = true,
  onInputDialog: OnInputDialog = defaultOnInputDialog,
) {
  Column(
    modifier = Modifier
      .settingModifier(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Column(
      modifier = Modifier,
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
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = "${
          BigDecimal(value.toDouble()).setScale(
            if (!showDecimal) 0 else 1,
            RoundingMode.HALF_EVEN
          )
        }",
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.labelMediumEmphasized,
        modifier = Modifier
          .clip(MaterialTheme.shapes.small)
          .background(MaterialTheme.colorScheme.primary)
          .height(16.dp)
          .widthIn(min = 24.dp)
          .width(16.dp + if (showDecimal) 16.dp else 0.dp + (max(valueRange.endInclusive.numPlaces(), value.numPlaces()) - 1) * 8.dp)
          .conditional(
            onInputDialog != defaultOnInputDialog,
            Modifier.clickable {
              onInputDialog(
                TextFieldDialogNavKey(
                  title = title,
                  description = description,
                  initValue = value.toString(),
                  onDone = { onValueChange(it.toFloatOrNull() ?: value) },
                  acceptableCharactersRegex = "[0-9.]"
                )
              )
            }
          ),
        textAlign = TextAlign.Center,
      )
      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
      )
    }
  }
}

@Composable
@FullPreview
fun SettingSliderPreview() {
  JetSnatcherTheme {
    var value by remember { mutableFloatStateOf(2f) }
    SettingSlider(
      title = "Preview setting",
      description = "Preview setting description. This text is supposed to describe what the option should do.",
      value = value,
      onValueChange = { value = it },
      valueRange = 1f..5f,
      steps = 3,
      showDecimal = false,
    )
  }
}