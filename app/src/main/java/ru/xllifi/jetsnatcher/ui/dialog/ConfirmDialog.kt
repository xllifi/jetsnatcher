package ru.xllifi.jetsnatcher.ui.dialog

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
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy.Companion.dialog
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.ignoreRoundedCorners
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Serializable
data class ConfirmDialogNavKey(
  val title: String,
  val description: String,
  val buttons: @Composable RowScope.(onDismiss: () -> Unit) -> Unit,
) : NavKey

fun EntryProviderScope<NavKey>.confirmDialogNavigation(
  backStack: NavBackStack<NavKey>,
) {
  entry<ConfirmDialogNavKey>(
    metadata = dialog() + ignoreRoundedCorners()
  )
  { key ->
    ConfirmDialog(
      title = key.title,
      description = key.description,
      buttons = key.buttons,
      onDismiss = {
        backStack.removeAt(backStack.lastIndex)
      }
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfirmDialog(
  title: String,
  description: String,
  onDismiss: () -> Unit,
  buttons: @Composable RowScope.(onDismiss: () -> Unit) -> Unit,
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
      buttons(onDismiss)
    }
  }
}

@Composable
@FullPreview
fun ConfirmDialogPreview() {
  JetSnatcherTheme {
    ConfirmDialog(
      title = "Are you sure?",
      description = "Confirm deletion of...",
      onDismiss = {}
    ) { onDismiss ->
      Button(
        onClick = {}
      ) {
        Text("Confirm")
      }
      Button(
        onClick = onDismiss
      ) {
        Text("Cancel")
      }
    }
  }
}