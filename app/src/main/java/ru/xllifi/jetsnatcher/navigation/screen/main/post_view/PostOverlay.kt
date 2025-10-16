package ru.xllifi.jetsnatcher.navigation.screen.main.post_view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.xllifi.booru_api.Post
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.isHorizontal
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModel
import ru.xllifi.jetsnatcher.posts
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostOverlay(
  modifier: Modifier = Modifier,
  postId: Int,
  show: Boolean,
  innerPadding: PaddingValues,
  postToolbarActions: PostToolbarActions = PostToolbarActions(),
  notesEnabled: Boolean,
) {
  val browserViewModel: BrowserViewModel = viewModel()
  val uiState by browserViewModel.uiState.collectAsState()
  val posts by remember { derivedStateOf { uiState.posts } }
  val post by remember { derivedStateOf { posts.first { it.id == postId } } }
  val tags: List<Any> by remember { derivedStateOf { post.tags ?: post.unparsedTags } }

  Box(modifier = modifier) {
    AnimatedVisibility(
      visible = show,
      enter = slideIn { intSize -> IntOffset(x = 0, y = -intSize.height) },
      exit = slideOut { intSize -> IntOffset(x = 0, y = -intSize.height) },
    ) {
      TopTagsRow(
        tags = tags,
        innerPadding = innerPadding,
      )
    }
    var toolbarExpanded by remember { mutableStateOf(false) }
    AnimatedVisibility(
      modifier = Modifier.align(Alignment.Companion.BottomEnd),
      visible = show,
      enter = slideIn { intSize -> IntOffset(x = 0, y = intSize.height) },
      exit = slideOut { intSize -> IntOffset(x = 0, y = intSize.height) },
    ) {
      PostToolbar(
        post = post,
        toolbarExpanded = toolbarExpanded,
        onClick = { toolbarExpanded = !toolbarExpanded },
        innerPadding = innerPadding,
        actions = postToolbarActions,
        notesEnabled = notesEnabled,
      )
    }
  }
}

@Composable
fun TopTagsRow(
  tags: List<Any>,
  innerPadding: PaddingValues,
) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = Modifier
      .pointerInteropFilter { false }
      .graphicsLayer(compositingStrategy = CompositingStrategy.Companion.Offscreen)
      .drawWithContent {
        drawContent()
        drawRect(
          brush = Brush.Companion.horizontalGradient(
            0.00f to Color.Companion.Black,
            0.04f to Color.Companion.Transparent,
            0.96f to Color.Companion.Transparent,
            1.00f to Color.Companion.Black,
          ),
          blendMode = BlendMode.Companion.DstOut,
        )
      }
      .padding(top = innerPadding.calculateTopPadding())
      .padding(vertical = 12.dp),
  ) {
    items(tags) {
      Tag(it) { label, value, fgColor, bgColor ->
        Text(
          text = label,
          color = Color.Companion.Black,
          modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(fgColor)
            .padding(horizontal = 16.dp, vertical = 4.dp),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@FullPreview
@Composable
fun TopTagsRowPreview() {
  JetSnatcherTheme {
    TopTagsRow(
      tags = listOf("tag1", "tag2", "tag3"),
      innerPadding = PaddingValues(0.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostToolbar(
  post: Post,
  toolbarExpanded: Boolean,
  onClick: () -> Unit,
  innerPadding: PaddingValues,
  actions: PostToolbarActions = PostToolbarActions(),
  notesEnabled: Boolean,
) {
  HorizontalFloatingToolbar(
    modifier = Modifier
      .conditional(
        !LocalWindowInfo.current.containerSize.isHorizontal(),
        Modifier.padding(bottom = innerPadding.calculateBottomPadding())
      )
      .padding(all = 32.dp),
    expanded = toolbarExpanded,
    floatingActionButton = {
      FloatingToolbarDefaults.StandardFloatingActionButton(
        onClick = onClick
      ) {
        Icon(
          imageVector = if (toolbarExpanded) Icons.Filled.ChevronRight else Icons.Filled.ChevronLeft,
          contentDescription = null,
        )
      }
    }
  ) {
    FilledIconButton(enabled = false, onClick = actions.onDownloadButtonPress) {
      Icon(
        imageVector = Icons.Outlined.Download,
        contentDescription = null,
      )
    }
    FilledIconButton(enabled = false, onClick = actions.onCommentButtonPress) {
      Icon(
        imageVector = Icons.AutoMirrored.Outlined.Comment,
        contentDescription = null,
      )
    }
    FilledIconButton(enabled = false, onClick = actions.onFavoriteButtonPress) {
      Icon(
        imageVector = Icons.Outlined.BookmarkBorder,
        contentDescription = null,
      )
    }
    FilledIconButton(onClick = { actions.onInfoButtonPress(post.id) }) {
      Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = null,
      )
    }
    FilledIconToggleButton(
      enabled = post.hasNotes,
      checked = notesEnabled,
      onCheckedChange = actions.onNotesButtonPress,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
        contentDescription = null,
      )
    }
  }
}

data class PostToolbarActions(
  val onDownloadButtonPress: () -> Unit = {},
  val onCommentButtonPress: () -> Unit = {},
  val onFavoriteButtonPress: () -> Unit = {},
  val onInfoButtonPress: (postId: Int) -> Unit = {},
  val onNotesButtonPress: (newVal: Boolean) -> Unit = {},
)

@FullPreview
@Composable
fun PostToolbarPreview() {
  JetSnatcherTheme {
    PostToolbar(
      post = posts.first(),
      toolbarExpanded = true,
      onClick = { },
      innerPadding = PaddingValues(0.dp),
      notesEnabled = false,
    )
  }
}