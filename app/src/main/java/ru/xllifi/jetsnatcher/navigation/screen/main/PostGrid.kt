package ru.xllifi.jetsnatcher.navigation.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Rating
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.R
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.plus
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme
import ru.xllifi.jetsnatcher.ui.theme.sizes

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.PostGrid(
  modifier: Modifier = Modifier,
  viewModel: BrowserViewModel,
  innerPadding: PaddingValues = PaddingValues.Zero,
  onScrolledToBottom: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsState().value
  val gridState = rememberLazyStaggeredGridState()
  LaunchedEffect(gridState.canScrollForward) {
    if (!gridState.canScrollForward) {
      onScrolledToBottom()
    }
  }
  SmartScrollToItemEffect(
    gridState = gridState,
    index = uiState.selectedPostIndex,
    postExpanded = uiState.expandPost,
  )
  val showPostInfo by LocalContext.current.settingsDataStore.data.map { it.showCardInfo }.collectAsState(true)
  LazyVerticalStaggeredGrid(
    state = gridState,
    modifier = modifier,
    columns = StaggeredGridCells.Adaptive(100.dp),
    verticalItemSpacing = 8.dp,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(8.dp) + innerPadding,
  ) {
    itemsIndexed(uiState.posts) { index, post ->
      val scope = rememberCoroutineScope()
      Card(
        modifier = Modifier
          .clip(RoundedCornerShape(MaterialTheme.sizes.roundingMedium))
          .clickable(!this@PostGrid.isTransitionActive) {
            viewModel.selectPost(index)
            scope.launch {
              awaitFrame()
              viewModel.expandPost(to = true)
            }
          },
        post = post,
        isVisible = !(uiState.expandPost && index == uiState.selectedPostIndex),
        isSelected = index == uiState.selectedPostIndex,
        showPostInfo = showPostInfo,
      )
    }
    item(
      span = StaggeredGridItemSpan.FullLine
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 24.dp, bottom = 64.dp),
        contentAlignment = Alignment.TopCenter,
      ) {
        AnimatedVisibility(
          visible = uiState.isLoadingNewPosts,
          enter = scaleIn() + fadeIn(),
          exit = scaleOut() + fadeOut(),
        ) {
          LoadingIndicator()
        }
        AnimatedVisibility(
          visible = uiState.noMorePosts,
          enter = scaleIn() + fadeIn(),
          exit = scaleOut() + fadeOut(),
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clip(MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.surfaceContainer)
              .padding(16.dp),
          ) {
            val context = LocalContext.current
            Text("No more posts!")
            Button(
              onClick = {
                GlobalScope.launch {
                  viewModel.loadPosts()
                }
              }
            ) {
              Text("Retry")
            }
          }
        }
      }
    }

  }
}

@OptIn(ExperimentalCoilApi::class, ExperimentalSharedTransitionApi::class)
@FullPreview
@Composable
fun PostGridPreview() {
  val previewHandler = AsyncImagePreviewHandler {
    ColorImage(color = 0xFF884444.toInt())
  }

  CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
    JetSnatcherTheme {
      SharedTransitionLayout {
        val browserViewModel = viewModel<BrowserViewModel>()
        PostGrid(
          viewModel = browserViewModel,
          onScrolledToBottom = {},
        )
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Card(
  modifier: Modifier = Modifier,
  post: Post,
  isVisible: Boolean,
  isSelected: Boolean,
  showPostInfo: Boolean,
) {
  val img = post.getImageForPreview()
  Box(
    modifier = Modifier
      .then(modifier)
      .clip(RoundedCornerShape(MaterialTheme.sizes.roundingMedium))
      .background(MaterialTheme.colorScheme.surfaceContainerLow)
  ) {
    AnimatedVisibility(
      visible = isVisible,
      enter = EnterTransition.None,
      exit = ExitTransition.None
    ) {
      Column(
        modifier = Modifier
//          .onPlaced {
//            // Please someone make a better solution this sucks
//            if (showPostInfo) {
//              browserViewModel.addIntSizeForPost(post.id, it.size)
//            }
//          }
          .conditional(
            isSelected,
            Modifier.sharedElement(
              sharedContentState = rememberSharedContentState(post.id),
              animatedVisibilityScope = this,
              clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(MaterialTheme.sizes.roundingMedium))
            )
          )
      ) {
        AsyncImage(
          modifier = Modifier
            .aspectRatio(img.width.toFloat() / img.height)
            .fillMaxSize()
          ,
          model = ImageRequest.Builder(LocalContext.current)
            .data(img.url)
            .size(width = img.width, height = img.height)
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
        )

        if (showPostInfo) {
          Row(
            modifier = Modifier.height(24.dp)
          ) {
            PreviewInfo(score = post.score) {
              if (post.unparsedTags.contains("absurdres")) {
                BadgeIcon(Icons.Outlined.HighQuality)
              }
              if (post.hasNotes) {
                BadgeIcon(Icons.AutoMirrored.Outlined.StickyNote2)
              }
              if (post.hasComments) {
                BadgeIcon(Icons.AutoMirrored.Outlined.Comment)
              }
            }
          }
        }
      }
    }
    if (!isVisible) {
      Column {
        Box(
          modifier = Modifier
            .aspectRatio(img.width.toFloat() / img.height)
            .fillMaxSize()
        ) {}
        if (showPostInfo) {
          Box(
            modifier = Modifier.height(24.dp)
          ) {}
        }
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedVisibilityScope.PreviewInfo(
  score: Int = 45,
  badges: @Composable () -> Unit,
) {
  Row(
    modifier = Modifier
      .animateEnterExit(
        exit = slideOut { size -> IntOffset(x = 0, y = size.height) },
        enter = slideIn { size -> IntOffset(x = 0, y = size.height) }
      )
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .padding(vertical = 4.dp, horizontal = 8.dp)
    ,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      Icon(
        imageVector = Icons.Outlined.ThumbUp,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .height(16.dp)
          .padding(vertical = 2.dp)
        ,
      )
      Text(
        text = "$score",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    Box(
      modifier = Modifier.weight(1f),
    )
    badges()
  }
}
@Composable
fun BadgeIcon(
  imageVector: ImageVector
) {
  Icon(
    imageVector,
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.size(16.dp),
  )
}


@OptIn(ExperimentalCoilApi::class, ExperimentalSharedTransitionApi::class)
@FullPreview
@Composable
fun CardPreview() {
  val drawable = ResourcesCompat.getDrawable(LocalResources.current, R.drawable.yui_preview, null)!!
  val previewHandler = AsyncImagePreviewHandler { drawable.asImage() }

  CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
    JetSnatcherTheme {
      Box(
        modifier = Modifier
          .width(120.dp)
      ) {
        SharedTransitionLayout {
          AnimatedVisibility(true) {
            Card(
              showPostInfo = true,
              post = Post(
                id = 0,
                rating = Rating.General,
                tags = null,
                unparsedTags = listOf(),
                score = 0,
                bestQualityImage = Image(
                  url = "",
                  width = 247,
                  height = 350
                ),
                mediumQualityImage = Image(
                  url = "",
                  width = 247,
                  height = 350
                ),
                worstQualityImage = Image(
                  url = "",
                  width = 247,
                  height = 350
                ),
                authorName = "",
                authorId = 0,
                changedAt = 0,
                createdAt = 0,
                hasNotes = true,
                hasComments = false,
                hasChildren = false,
              ),
              isSelected = true,
              isVisible = true,
            )
          }
        }
      }
    }
  }
}