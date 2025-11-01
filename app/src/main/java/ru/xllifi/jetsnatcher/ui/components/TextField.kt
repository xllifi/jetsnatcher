package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.extensions.conditional

@Composable
fun TextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (newVal: String) -> Unit,
  onKeyboardDone: (value: String) -> Unit,
  icon: ImageVector? = null,
  placeholder: String? = null,
  label: String? = null,
  acceptableCharactersRegex: Regex? = null,
  singleLine: Boolean = false,
) {
  BasicTextField(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.small)
      .background(MaterialTheme.colorScheme.surfaceContainer),
    value = value,
    onValueChange = { value ->
      if (acceptableCharactersRegex != null) {
        onValueChange(value.filter { it.toString().matches(acceptableCharactersRegex) })
      } else {
        onValueChange(value)
      }
    },
    singleLine = singleLine,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions { onKeyboardDone(value) },
    textStyle = MaterialTheme.typography.bodyLarge.copy(
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 16.sp,
      lineHeight = 16.sp,
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
    decorationBox = { innerTextField ->
      if (label != null) {
        val labelSp = with(LocalDensity.current) {
          12.dp.toSp()
        }
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
            fontSize = labelSp,
            lineHeight = labelSp,
          ),
          modifier = Modifier
            .zIndex(2f)
            .offset(x = 12.dp, y = 2.dp),
        )
      }
      Row(
        modifier = Modifier
          .padding(horizontal = 12.dp, vertical = 12.dp)
          .conditional(
            condition = label != null,
            ifTrue = Modifier.padding(top = 6.dp),
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }
        Row {
          if (value.isEmpty() && placeholder?.isNotEmpty() ?: false) {
            Text(
              text = placeholder,
              style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                fontSize = 16.sp,
                lineHeight = 16.sp,
              ),
            )
          }
          innerTextField()
        }
      }
    },
  )
}

@FullPreview
@Composable
private fun TextFieldPreview() {
  PreviewSetup {
    var value by remember { mutableStateOf("Something nsgjgdnkfgndgbsdfbuiherwfgdjkddsg5redf...") }
    TextField(
      modifier = Modifier,
      value = value,
      onValueChange = { value = it },
      onKeyboardDone = { value = it },
      icon = Icons.Default.TextFields,
      label = "Label",
      acceptableCharactersRegex = Regex("[a-zA-Z0-9]"),
      singleLine = true,
    )
  }
}