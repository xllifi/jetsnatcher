package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme


@Composable
fun ConfirmDialog(
  title: String,
  description: String,
  onDismissRequest: () -> Unit,
  buttons: @Composable RowScope.() -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
  ) {
    ConfirmDialogContent(
      title = title,
      description = description,
      buttons = buttons,
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmDialogContent(
  title: String,
  description: String,
  buttons: @Composable RowScope.() -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .verticalScroll(rememberScrollState())
      .padding(12.dp),
  ) {
    Text(
      text = title,
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(bottom = 16.dp),
    )
    Text(
      text = description,
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(bottom = 16.dp),
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
      buttons()
    }
  }
}

@Composable
@FullPreview
fun ConfirmDialogPreview() {
  JetSnatcherTheme {
    ConfirmDialogContent(
      title = "Are you sure?",
      description = "Confirm deletion of...",
    ) {
      Button(
        onClick = {}
      ) {
        Text("Confirm")
      }
      Button(
        onClick = {}
      ) {
        Text("Cancel")
      }
    }
  }
}