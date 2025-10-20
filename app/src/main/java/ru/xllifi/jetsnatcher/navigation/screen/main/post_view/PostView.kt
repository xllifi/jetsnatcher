package ru.xllifi.jetsnatcher.navigation.screen.main.post_view

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import ru.xllifi.jetsnatcher.extensions.AnimateCornerSize
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.isHorizontal
import ru.xllifi.jetsnatcher.extensions.pxToDp
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PostView(
  modifier: Modifier = Modifier,
  viewModel: BrowserViewModel,
  animatedVisibilityScope: AnimatedVisibilityScope,
  onBack: () -> Unit,
  /** Swipe right/left */
  onSelectedPostChange: (newIndex: Int) -> Unit,
  innerPadding: PaddingValues,
  postToolbarActions: PostToolbarActions = PostToolbarActions(),
) {
  val windowSize = LocalWindowInfo.current.containerSize
  val uiState by viewModel.uiState.collectAsState()

  // region predictive back
  var predictiveBackProgress by remember { mutableFloatStateOf(1f) }
  PredictiveBackHandler { progress ->
    try {
      progress.collect { backEvent ->
        predictiveBackProgress = 1f - backEvent.progress
      }
      predictiveBackProgress = 0f
      onBack()
    } catch (e: CancellationException) {
      predictiveBackProgress = 1f
      throw e
    }
  }
  // endregion

  // region drag
  var dragToDismissProgress by remember { mutableFloatStateOf(1f) }
  // endregion

  // region resulting values
  var alpha by remember { mutableFloatStateOf(1f) }
  LaunchedEffect(predictiveBackProgress, dragToDismissProgress) {
    alpha = (predictiveBackProgress * dragToDismissProgress) * 5 - 4
  }

  var scale by remember { mutableFloatStateOf(1f) }
  LaunchedEffect(predictiveBackProgress, dragToDismissProgress) {
    scale = (predictiveBackProgress * dragToDismissProgress) * 0.4f + 0.6f
  }

  var cornerSize by remember { mutableStateOf(0.dp) }
  LaunchedEffect(predictiveBackProgress, dragToDismissProgress) {
    cornerSize = ((1f - predictiveBackProgress * dragToDismissProgress) * 128).coerceAtMost(12f).dp
  }
  // endregion

  var pagerScrollEnabled by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.scrim.copy(alpha))
  ) {
    val pagerState = rememberPagerState(
      initialPage = uiState.selectedPostIndex,
      pageCount = { uiState.posts.size },
    )
    LaunchedEffect(pagerState.currentPage) {
      onSelectedPostChange(pagerState.currentPage)
    }
    HorizontalPager(
      modifier = Modifier
        // A hack to make touch work when transition is active
        .conditional(
          !uiState.expandPost,
          Modifier.offset(x = -windowSize.height.pxToDp())
        ),
      state = pagerState,
      userScrollEnabled = pagerScrollEnabled,
    ) { index ->
      AllGestures(
        modifier = Modifier.align(Alignment.Center),
        predictiveBackProgress = predictiveBackProgress,
        onScaleChange = { pagerScrollEnabled = it <= 1f },
        onDismiss = onBack,
        onDragToDismissValue = { dragToDismissProgress = it }
      ) { scaleValue, offsetValue, scaleModifier ->
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          var showNotes by remember { mutableStateOf(false) }
          // Image
          PostImage(
            modifier = scaleModifier
              .conditional( // Predictive back & drag-to-dismiss shrinking
                windowSize.isHorizontal(),
                Modifier.height(windowSize.height.pxToDp() * scale),
                Modifier.width(windowSize.width.pxToDp() * scale)
              )
              .graphicsLayer(
                scaleX = scaleValue,
                scaleY = scaleValue,
                translationX = offsetValue.x * scaleValue,
                translationY = offsetValue.y * scaleValue,
                transformOrigin = TransformOrigin(0f, 0f)
              ),
            viewModel = viewModel,
            postIndex = index,
            isSelected = uiState.selectedPostIndex == index,
            animatedVisibilityScope = animatedVisibilityScope,
            cornerSize = cornerSize,
            innerPadding = innerPadding,
            alpha = alpha,
            showNotes = showNotes,
          )
          // Info overlay
          PostOverlay(
            modifier = Modifier
              .fillMaxSize()
              .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 2f)
              .alpha(alpha),
            show = scaleValue <= 1f,
            viewModel = viewModel,
            postIndex = index,
            innerPadding = innerPadding,
            postToolbarActions = postToolbarActions.copy(
              onNotesButtonPress = { showNotes = !showNotes }
            ),
            showNotes = showNotes,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.PostImage(
  modifier: Modifier = Modifier,
  viewModel: BrowserViewModel,
  postIndex: Int,
  isSelected: Boolean,
  animatedVisibilityScope: AnimatedVisibilityScope,
  cornerSize: Dp,
  innerPadding: PaddingValues,
  alpha: Float,
  showNotes: Boolean,
) {
  val uiState by viewModel.uiState.collectAsState()
  val post by remember { derivedStateOf { uiState.posts[postIndex] } }

  if ((post.hasNotes && post.notes == null) || post.tags == null) {
    LaunchedEffect(Unit) {
      viewModel.loadPostMeta(postIndex)
    }
  }

  val previewImage = post.getImageForPreview()
  val img = post.getImageForFullscreen()

  var aspectRatio = img.width.toFloat() / img.height.toFloat()
  if (aspectRatio == 0f) {
    aspectRatio = 0.01f
  }
  Box(modifier = modifier) {
    val context = LocalContext.current
    AnimateCornerSize(
      animatedVisibilityScope,
      sizeWhenHidden = 16.dp,
    ) { size ->
      val modifier = Modifier
        .aspectRatio(aspectRatio)
        .fillMaxSize()
        .align(Alignment.Center)
      var isLoading by remember { mutableStateOf(false) }
      var imageSize by remember { mutableStateOf(IntSize.Zero) }

      var shownNote by remember { mutableIntStateOf(-1) }

      AsyncImage(
        modifier = modifier
          .onSizeChanged { imageSize = it }
          .conditional(
            isSelected,
            Modifier.sharedElement(
              sharedContentState = rememberSharedContentState(post.id),
              animatedVisibilityScope = animatedVisibilityScope,
              clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(size))
            ),
          )
          .clip(RoundedCornerShape(cornerSize))
          .pointerInput(Unit) {
            awaitEachGesture {
              val down = awaitFirstDown()
              if (down.pressed) {
                shownNote = -1
              }
            }
          },
        model = ImageRequest.Builder(context)
          .data(img.url)
          .size(width = img.width, height = img.height)
          .crossfade(false)
          .build(),
        contentDescription = null,
        onLoading = { isLoading = true },
        onSuccess = { isLoading = false }
      )

      // region notes
      if (post.hasNotes && showNotes && post.notes != null) {
        LaunchedEffect(alpha) {
          if (alpha < 1f) {
            shownNote = -1
          }
        }
        RenderNotes(
          post = post,
          imageSize = imageSize,
          shownNote = shownNote,
          onShownNoteChange = { shownNote = it }
        )
      }
      // endregion

      if (isLoading) {
        AsyncImage(
          modifier = modifier
            .conditional(
              isSelected,
              Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(post.id),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = EnterTransition.None,
                exit = ExitTransition.None,
              ),
            )
            .clip(RoundedCornerShape(cornerSize)),
          model = ImageRequest.Builder(context)
            .data(previewImage.url)
            .size(width = previewImage.width, height = previewImage.height)
            .build(),
          contentDescription = null
        )
      }

      AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = isLoading,
        enter = EnterTransition.None,
        exit = fadeOut(),
      ) {
        Box(
          modifier = Modifier.fillMaxSize()
        ) {
          ContainedLoadingIndicator(
            modifier = Modifier
              .padding(top = innerPadding.calculateTopPadding() + 56.dp)
              .width(40.dp)
              .height(40.dp)
              .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 5f)
              .align(Alignment.TopCenter)
              .alpha(alpha)
          )
        }
      }
    }
  }
}