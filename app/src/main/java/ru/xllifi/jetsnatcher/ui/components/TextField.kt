package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (newVal: String) -> Unit,
  onKeyboardDone: (value: String) -> Unit,
  icon: ImageVector? = null,
  textAlign: TextAlign = TextAlign.Start,
  acceptableCharactersRegex: Regex? = null,
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
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions { onKeyboardDone(value) },
    textStyle = MaterialTheme.typography.bodyLarge.copy(
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 16.sp,
      lineHeight = 16.sp,
      textAlign = textAlign,
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
        Box {
          if (value.isEmpty()) {
            Text(
              text = "Search tags...", // TODO: translate
              style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                fontSize = 16.sp,
                lineHeight = 16.sp,
                textAlign = textAlign,
              ),
            )
          }
          innerTextField()
        }
      }
    }
  )
}