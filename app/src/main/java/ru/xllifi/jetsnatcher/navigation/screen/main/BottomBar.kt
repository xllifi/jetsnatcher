package ru.xllifi.jetsnatcher.navigation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetupBrowserViewModel
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Composable
fun BottomBar(
  viewModel: BrowserViewModel,
  modifier: Modifier = Modifier,
  innerPadding: PaddingValues,
  onClick: () -> Unit,
) {
  val pad = PaddingValues(
    top = 0.dp,
    bottom = innerPadding.calculateBottomPadding(),
    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
  )
  val uiState by viewModel.uiState.collectAsState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .clickable { onClick() }
      .padding(pad)
      .height(64.dp)
      .padding(8.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    LazyRow(
      modifier = Modifier
        .weight(1f)
        .height(64.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerLow)
        .padding(8.dp)
        .clip(CircleShape),
      horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items(viewModel.searchTags) { tag ->
        Tag(
          tag,
          baseFgColor = MaterialTheme.colorScheme.onPrimary,
          baseBgColor = MaterialTheme.colorScheme.primary,
        ) { label, value, fgColor, bgColor ->
          Box(Modifier.animateItem()) {
            Text(
              text = label,
              color = fgColor,
              modifier = Modifier
                .clip(CircleShape)
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }
  }
}

@FullPreview
@Composable
fun BottomBarPreview() {
  PreviewSetupBrowserViewModel { viewModel ->
    BottomBar(
      viewModel = viewModel,
      innerPadding = PaddingValues.Zero,
      onClick = {},
    )
  }
}