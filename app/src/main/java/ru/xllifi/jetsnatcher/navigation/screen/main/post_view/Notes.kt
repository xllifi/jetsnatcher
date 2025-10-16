package ru.xllifi.jetsnatcher.navigation.screen.main.post_view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ireward.htmlcompose.HtmlText
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.Post
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.pxToDp

@Composable
fun BoxScope.RenderNotes(
  post: Post,
  imageSize: IntSize,
  shownNote: Int,
  onShownNoteChange: (newIndex: Int) -> Unit
) {
  Box(
    modifier = Modifier.Companion
      .align(Alignment.Companion.Center)
      .size(
        width = imageSize.width.pxToDp(),
        height = imageSize.height.pxToDp(),
      )
  ) {
    val bqImage = post.bestQualityImage
    (post.notes as Iterable<Note>).forEachIndexed { index, note ->
      val xOffsetPercent = note.x.toFloat() / bqImage.width.toFloat()
      val xOffsetPx = imageSize.width * xOffsetPercent
      val widthPx = imageSize.width * (note.width.toFloat() / bqImage.width.toFloat())
      val yOffsetPercent = note.y.toFloat() / bqImage.height.toFloat()
      val yOffsetPx = imageSize.height * yOffsetPercent
      val heightPx = imageSize.height * (note.height.toFloat() / bqImage.height.toFloat())

      Box(
        modifier = Modifier.Companion
          .zIndex(1f)
          .size(
            width = widthPx.pxToDp(),
            height = heightPx.pxToDp(),
          )
          .absoluteOffset(
            x = xOffsetPx.pxToDp(),
            y = yOffsetPx.pxToDp(),
          )
          .clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
          ) { onShownNoteChange(index) }
          .clip(MaterialTheme.shapes.extraSmall)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.extraSmall,
          )
          .background(MaterialTheme.colorScheme.primary.copy(0.4f))
      ) {}
      if (shownNote == index) {
        var size by remember { mutableStateOf(IntSize.Companion.Zero) }
        val offsetX by remember {
          derivedStateOf {
            if (xOffsetPx > imageSize.width / 2) {
              xOffsetPx + widthPx - size.width
            } else {
              xOffsetPx
            }
          }
        }
        val offsetY by remember {
          derivedStateOf {
            if (yOffsetPx > imageSize.height / 2) {
              yOffsetPx - size.height
            } else {
              yOffsetPx + heightPx
            }
          }
        }
        Box(
          modifier = Modifier.Companion
            .zIndex(2f)
            .onPlaced { size = it.size }
            .absoluteOffset(
              x = offsetX.pxToDp(),
              y = offsetY.pxToDp(),
            )
            .widthIn(max = (imageSize.width / 2).pxToDp())
            .conditional(
              size == IntSize.Companion.Zero,
              Modifier.Companion.alpha(0f)
            )
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          val uriHandler = LocalUriHandler.current
          HtmlText(
            text = note.body.replace(Regex("<[^>]*>"), ""),
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            linkClicked = { uriHandler.openUri(it) },
          )
        }
      }
    }
  }
}