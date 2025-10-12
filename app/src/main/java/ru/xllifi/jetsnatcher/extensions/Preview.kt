package ru.xllifi.jetsnatcher.extensions

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

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