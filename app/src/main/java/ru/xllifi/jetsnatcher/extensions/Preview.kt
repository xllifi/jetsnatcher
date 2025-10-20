package ru.xllifi.jetsnatcher.extensions

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModel
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModelFactory
import ru.xllifi.jetsnatcher.navigation.screen.main.post_details.PostDetails
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
  locale = "ar", // for RTL layout
)
@Preview(
  locale = "ru",
)
annotation class FullPreview

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  showSystemUi = true,
)
@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
  locale = "ar",
  showSystemUi = true,
)
@Preview(
  locale = "en",
  showSystemUi = true,
)
annotation class FullPreviewSysUi

@Composable
fun PreviewSetup(
  content: @Composable () -> Unit
) {
  JetSnatcherTheme {
    content()
  }
}

@Composable
fun PreviewSetupBrowserViewModel(
  content: @Composable (viewModel: BrowserViewModel) -> Unit
) {
  PreviewSetup {
    val context = LocalContext.current
    val viewModel: BrowserViewModel = viewModel(
      factory = BrowserViewModelFactory(
        context,
        ProviderProto(),
        emptyList()
      )
    )
    content(viewModel)
  }
}