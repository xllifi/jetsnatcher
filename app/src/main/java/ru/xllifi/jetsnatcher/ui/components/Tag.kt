package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.ajalt.colormath.extensions.android.composecolor.toColormathColor
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

/**
 * Parses [ru.xllifi.booru_api.Tag] or [String] to label and value and tints input `baseFgColor` and `baseBgColor` into the tag category's hue.
 *
 * If `tag` is a string, removes saturation.
 * */
@Composable
fun Tag(
  tag: Any,
  baseFgColor: Color = MaterialTheme.colorScheme.onBackground,
  baseBgColor: Color = MaterialTheme.colorScheme.background,
  content: @Composable (label: String, value: String, fgColor: Color, bgColor: Color) -> Unit,
) {
  val label = when (tag) {
    is Tag -> tag.label
    is String -> tag.replace("_", " ")
    else -> throw IllegalArgumentException("Unknown tag type")
  }
  val bgHsl = baseBgColor.toColormathColor().toHSL()
  val bgColor = when (tag) {
    is Tag -> Color.hsl(
      hue = tag.category.hue(),
      saturation = bgHsl.s,
      lightness = bgHsl.l,
    )
    else -> Color.hsl(
      hue = 0f,
      saturation = 0f,
      lightness = bgHsl.l,
    )
  }
  val fgHsl = baseFgColor.toColormathColor().toHSL()
  val fgColor = when (tag) {
    is Tag -> Color.hsl(
      hue = tag.category.hue(),
      saturation = fgHsl.s,
      lightness = fgHsl.l,
    )
    else -> Color.hsl(
      hue = 0f,
      saturation = 0f,
      lightness = fgHsl.l,
    )
  }
  val value = when (tag) {
    is Tag -> tag.value
    is String -> tag
    else -> throw IllegalArgumentException("Unknown tag type")
  }
  content(label, value, fgColor, bgColor)
}

@Composable
@FullPreview
fun TagPreview() {
  JetSnatcherTheme {
    Tag(
      tag = Tag(
        label = "hirasawa yui",
        value = "hirasawa_yui",
        postCount = 1234,
        category = TagCategory.Character
      )
    ) { label, value, fgColor, bgColor ->
      Text(
        text = label,
        color = fgColor,
        modifier = Modifier
          .background(bgColor)
          .padding(8.dp)
      )
    }
  }
}