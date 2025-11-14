package ru.xllifi.jetsnatcher.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
data class ListSelectDialogNavKey<T>(
  val onSelect: (value: T) -> Unit,
  val items: ListSelectDialogScope<T>.() -> Unit,
) : NavKey

fun EntryProviderScope<NavKey>.listSelectDialogNavigation(
  backStack: NavBackStack<NavKey>,
) {
  entry<ListSelectDialogNavKey<Any>>(
    metadata = dialog() + ignoreRoundedCorners()
  )
  { key ->
    ListSelectDialog(
      onSelect =  { providerType ->
        key.onSelect(providerType)
        backStack.removeAt(backStack.lastIndex)
      },
      items = key.items
    )
  }
}

interface ListSelectDialogScope<T> {
  data class Item<T>(
    val value: T,
    val label: String,
  )

  fun item(value: T, label: String)
  fun items(items: List<Pair<T, String>>)
  fun items(values: List<T>, labels: List<String>)
}

class ListSelectDialogScopeImpl<T> : ListSelectDialogScope<T> {
  val items = mutableListOf<ListSelectDialogScope.Item<T>>()

  override fun item(value: T, label: String) {
    items.add(ListSelectDialogScope.Item(value, label))
  }

  override fun items(items: List<Pair<T, String>>) {
    this.items.addAll(items.map { ListSelectDialogScope.Item(it.first, it.second) })
  }

  override fun items(values: List<T>, labels: List<String>) {
    this.items.addAll(values.mapIndexed { i, el -> ListSelectDialogScope.Item(el, labels[i]) })
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun<T> ListSelectDialog(
  onSelect: (value: T) -> Unit,
  items: ListSelectDialogScope<T>.() -> Unit,
) {
  val scope = remember { ListSelectDialogScopeImpl<T>() }
  scope.items()
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
      text = "Select provider type",
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(bottom = 16.dp),
    )
    LazyColumn(
      modifier = Modifier
        .heightIn(max = 200.dp)
        .clip(MaterialTheme.shapes.small),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(scope.items) { item ->
        Text(
          text = item.label,
          color = MaterialTheme.colorScheme.onPrimary,
          style = MaterialTheme.typography.labelLargeEmphasized,
          modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onSelect(item.value) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
@FullPreview
private fun ListSelectDialogPreview() {
  JetSnatcherTheme {
    ListSelectDialog(
      onSelect = { }
    ) {
      item("value1", "label1")
    }
  }
}