package ru.xllifi.jetsnatcher.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@Suppress("FunctionName")
public fun <T : Any> RoundedCornerNavEntryDecorator(): NavEntryDecorator<T> {
  val decorator: @Composable (entry: NavEntry<T>) -> Unit = { entry ->
    var color = entry.metadata["BACKGROUND_COLOR"]
    if (color == null || color !is Color) {
      color = MaterialTheme.colorScheme.background
    }
    AnimateCornerSize(
      animatedVisibilityScope = LocalNavAnimatedContentScope.current,
      sizeWhenHidden = 48.dp,
      sizeWhenVisible = 8.dp
    ) { size ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(size))
          .background(color)
      ) {
        entry.Content()
      }
    }
  }

  return navEntryDecorator(decorator = decorator)
}

@Composable
public fun <T : Any> rememberRoundedCornerNavEntryDecorator(): NavEntryDecorator<T> = remember {
  RoundedCornerNavEntryDecorator()
}