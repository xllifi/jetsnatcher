package ru.xllifi.jetsnatcher.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup

interface SettingsPageScope {
  fun group(
    title: (@Composable (modifier: Modifier) -> Unit)?,
    content: SettingsGroupScope.() -> Unit,
  )

  fun group(
    title: String,
    content: SettingsGroupScope.() -> Unit,
  )
}

class SettingsPageScopeImpl : SettingsPageScope {
  val groups = mutableListOf<@Composable () -> Unit>()

  override fun group(
    title: @Composable ((modifier: Modifier) -> Unit)?,
    content: (SettingsGroupScope.() -> Unit)
  ) {
    groups.add({
      SettingsGroup(
        title = title,
        content = content
      )
    })
  }

  override fun group(
    title: String,
    content: (SettingsGroupScope.() -> Unit)
  ) {
    groups.add({
      SettingsGroup(
        title = title,
        content = content
      )
    })
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
  title: String,
  onBack: () -> Unit,
  content: SettingsPageScope.() -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
              ),
            tooltip = { PlainTooltip { Text("Menu") } },
            state = rememberTooltipState(),
          ) {
            IconButton(onClick = onBack) {
              Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Menu")
            }
          }
        },
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .padding(innerPadding),
    ) {
      val scope = remember { SettingsPageScopeImpl() }
      scope.groups.clear()
      scope.content()
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items(scope.groups) { group ->
          group()
        }
      }
    }
  }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@FullPreview
private fun SettingsPagePreview() {
  PreviewSetup {
    var checked by remember { mutableStateOf(false) }
    SettingsPage(
      title = "Settings page",
      onBack = {},
    ) {
      group("Some group") {
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
      }
      group("Another group") {
        settingSlider(
          title = "Slider setting",
          description = "For number settings",
          value = 15f,
          onValueChange = {},
          valueRange = 0f..30f,
        )
        settingButton(
          title = "Travel setting",
          description = "Takes you to another screen",
          onClick = {}
        )
      }
    }
  }
}