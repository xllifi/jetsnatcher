package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Serializable
data class TextFieldDialogNavKey(
  val title: String,
  val description: String?,
  val initValue: String,
  val onDone: (String) -> Unit,
  val acceptableCharactersRegex: String? = null,
) : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextFieldDialog(
  title: String,
  description: String?,
  initValue: String,
  onDone: (String) -> Unit,
  acceptableCharactersRegex: Regex? = null,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .verticalScroll(rememberScrollState())
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = title,
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.titleMedium,
    )
    if (description != null) {
      Text(
        text = description,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    var value by remember { mutableStateOf(initValue) }
    TextField(
      value = value,
      onValueChange = { value = it },
      onKeyboardDone = onDone,
      icon = null,
      acceptableCharactersRegex = acceptableCharactersRegex,
    )
    Button(
      onClick = { onDone(value) },
    ) {
      Text("Done")
    }
  }
}

@Composable
@FullPreview
fun TextFieldDialogPreview() {
  JetSnatcherTheme {
    TextFieldDialog(
      title = "Are you sure?",
      description = "Confirm deletion of...",
      initValue = "Some value",
      onDone = {},
    )
  }
}