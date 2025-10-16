package ru.xllifi.jetsnatcher.navigation.screen.main

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ireward.htmlcompose.HtmlText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.Post
import ru.xllifi.jetsnatcher.extensions.AnimateCornerSize
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.extensions.isHorizontal
import ru.xllifi.jetsnatcher.extensions.pxToDp
import ru.xllifi.jetsnatcher.posts
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PostView(
  modifier: Modifier = Modifier,
  browserViewModel: BrowserViewModel,
  animatedVisibilityScope: AnimatedVisibilityScope,
  onBack: () -> Unit,
  /** Swipe right/left */
  onSelectedPostChange: (newIndex: Int) -> Unit,
  innerPadding: PaddingValues,
  postToolbarActions: PostToolbarActions = PostToolbarActions(),
) {
  val windowSize = LocalWindowInfo.current.containerSize

  val uiState by browserViewModel.uiState.collectAsState()
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
      val post by remember { derivedStateOf { uiState.posts[index] } }
      val isSelected by remember { derivedStateOf { uiState.selectedPostIndex == index } }
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
          var notesEnabled by remember { mutableStateOf(false) }
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
            postId = post.id,
            isSelected = isSelected,
            animatedVisibilityScope = animatedVisibilityScope,
            cornerSize = cornerSize,
            innerPadding = innerPadding,
            alpha = alpha,
            notesEnabled = notesEnabled,
          )
          // Info overlay
          PostInfo(
            modifier = Modifier
              .fillMaxSize()
              .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 2f)
              .alpha(alpha),
            postId = post.id,
            show = scaleValue <= 1f,
            innerPadding = innerPadding,
            postToolbarActions = postToolbarActions.copy(
              onNotesButtonPress = { notesEnabled = !notesEnabled }
            ),
            notesEnabled = notesEnabled,
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
  postId: Int,
  isSelected: Boolean,
  animatedVisibilityScope: AnimatedVisibilityScope,
  cornerSize: Dp,
  innerPadding: PaddingValues,
  alpha: Float,
  notesEnabled: Boolean,
) {
  val browserViewModel: BrowserViewModel = viewModel()
  val uiState by browserViewModel.uiState.collectAsState()
  val posts by remember { derivedStateOf { uiState.posts } }
  val post by remember { derivedStateOf { posts.first { it.id == postId } } }

  if ((post.hasNotes && post.notes == null) || post.tags == null) {
    LaunchedEffect(Unit) {
      browserViewModel.loadTagsAndNotes(postId)
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
      if (post.hasNotes && notesEnabled && post.notes != null) {
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

@Composable
private fun BoxScope.RenderNotes(
  post: Post,
  imageSize: IntSize,
  shownNote: Int,
  onShownNoteChange: (newIndex: Int) -> Unit
) {
  Box(
    modifier = Modifier
      .align(Alignment.Center)
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
        modifier = Modifier
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
        var size by remember { mutableStateOf(IntSize.Zero) }
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
          modifier = Modifier
            .zIndex(2f)
            .onPlaced { size = it.size }
            .absoluteOffset(
              x = offsetX.pxToDp(),
              y = offsetY.pxToDp(),
            )
            .widthIn(max = (imageSize.width / 2).pxToDp())
            .conditional(
              size == IntSize.Zero,
              Modifier.alpha(0f)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostInfo(
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
      modifier = Modifier.align(Alignment.BottomEnd),
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
      .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
      .drawWithContent {
        drawContent()
        drawRect(
          brush = Brush.horizontalGradient(
            0.00f to Color.Black,
            0.04f to Color.Transparent,
            0.96f to Color.Transparent,
            1.00f to Color.Black,
          ),
          blendMode = BlendMode.DstOut,
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
          modifier = Modifier.Companion
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
private fun PostToolbar(
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

@Composable
private fun AllGestures(
  modifier: Modifier = Modifier,
  doubleTapScale: Float = 2f,
  maxScale: Float = 4f,
  predictiveBackProgress: Float,
  onScaleChange: (scale: Float) -> Unit,
  onDismiss: () -> Unit,
  onDragToDismissValue: (onDragToDismissValue: Float) -> Unit,
  content: @Composable (scaleValue: Float, offsetValue: Offset, scaleModifier: Modifier) -> Unit,
) {
  val windowSize = LocalWindowInfo.current.containerSize
  val dragThreshold = windowSize.height / 4f
  val maxDragToTop = windowSize.height / 10f

  var isPressed by remember { mutableStateOf(false) }

  var velocityY by remember { mutableFloatStateOf(0f) }
  var dragToDismiss by remember { mutableFloatStateOf(0f) }
  val dragToDismissValue by animateFloatAsState(
    targetValue = dragToDismiss,
    animationSpec = if (isPressed) {
      snap()
    } else {
      spring()
    }
  )
  var offset by remember { mutableStateOf(Offset.Zero) }
  val offsetValue by animateOffsetAsState(
    targetValue = offset,
    animationSpec = if (isPressed) {
      snap()
    } else {
      spring()
    }
  )
  var scale by remember { mutableFloatStateOf(1f) }
  val scaleValue by animateFloatAsState(
    targetValue = scale,
    animationSpec = if (isPressed) {
      snap()
    } else {
      spring()
    }
  )
  var intSize: IntSize = IntSize.Zero
  var globalPosition: Offset = Offset.Zero

  LaunchedEffect(predictiveBackProgress) {
    if (predictiveBackProgress > 0f) {
      scale = 1f
      offset = Offset.Zero
      dragToDismiss = 0f
    }
  }

  LaunchedEffect(offset, scale, dragToDismissValue, isPressed) {
    if (scale <= 1f) {
      dragToDismiss = offset.y.coerceIn(0f, dragThreshold) / dragThreshold
      onDragToDismissValue(1f - dragToDismissValue)
      if (!isPressed) {
        if (
          velocityY > 60f // Is swiping down
          ||
          offset.y > windowSize.height / 6 // Has dragged too far down
        ) {
          onDismiss()
        }
      }
    } else {
      dragToDismiss = 0f
      onDragToDismissValue(1f - dragToDismissValue)
    }

    if (isPressed) return@LaunchedEffect
    // Scale
    if (scale < 1f) {
      scale = 1f
    }
    // Offset
    if (scale <= 1f) {
      offset = Offset.Zero
    } else {
      // region prepare
      var retX = offset.x
      var retY = offset.y

      val isHorizontal = windowSize.isHorizontal()
      val isVertical = !isHorizontal
      // endregion

      // region calculate X
      val minX = -(intSize.width + (globalPosition.x - windowSize.width) / scale)
      val centerX = (intSize.width / scale - intSize.width) / 2
      val maxX = -(globalPosition.x / scale)
      retX = if ((isVertical || intSize.width * scale > windowSize.width) && maxX > minX) {
        retX.coerceIn(minX, maxX)
      } else {
        centerX
      }
      // endregion

      // region calculate Y
      val minY = -(intSize.height + (globalPosition.y - windowSize.height) / scale)
      val centerY = (intSize.height / scale - intSize.height) / 2
      val maxY = -(globalPosition.y / scale)
      retY = if ((isHorizontal || intSize.height * scale > windowSize.height) && maxY > minY) {
        retY.coerceIn(minY, maxY)
      } else {
        centerY
      }
      // endregion

      offset = offset.copy(x = retX, y = retY)
    }
  }
  LaunchedEffect(scale) {
    onScaleChange(scale)
  }

  val context = LocalContext.current
  val settings = runBlocking { context.settingsDataStore.data.first() }

  Box(
    modifier = modifier
      .pointerInput(Unit) {
        var firstTapTime = 0L
        var tapCount = 0
        awaitEachGesture {
          while (true) {
            val event = awaitUnconsumedPointerEvent()
            isPressed = event.changes.any { it.pressed }

            // Handle double tap gesture
            val doubleTapResult = handleDoubleTap(
              event = event,
              doubleTapThreshold = settings.doubleTapThreshold,
              tapCount = tapCount,
              firstTapTime = firstTapTime,
              currentScale = scale,
              doubleTapScale = doubleTapScale,
              globalPosition = globalPosition,
              windowSize = windowSize,
            )
            tapCount = doubleTapResult.tapCount
            firstTapTime = doubleTapResult.firstTapTime
            if (doubleTapResult.detected) {
              isPressed = false
              scale = doubleTapResult.scale
              offset = doubleTapResult.offset
              event.changes.forEach { it.consume() }
              continue
            }

            // Reset tap count if too much time has passed since the first tap
            if (tapCount == 1 && System.currentTimeMillis() - firstTapTime >= 300) {
              tapCount = 0
            }

            // Ignore if horizontal drag
            if (isHorizontalDrag(scale, offset, event)) {
              return@awaitEachGesture
            }

            event.changes.forEach { it.consume() }

            // Handle transformations (zoom, pan, drag)
            val transformResult = handleZoomPanDrag(
              event = event,
              currentScale = scale,
              currentOffset = offset,
              maxScale = maxScale,
              maxDragToTop = maxDragToTop,
              globalPosition = globalPosition,
            )
            scale = transformResult.scale
            offset = transformResult.offset
            velocityY = transformResult.velocityY
          }
        }
      }
  ) {
    content(
      scaleValue,
      offsetValue,
      Modifier
        .onSizeChanged { intSize = it }
        .onGloballyPositioned { globalPosition = it.positionInWindow() },
    )
  }
}

private suspend fun AwaitPointerEventScope.awaitUnconsumedPointerEvent(pass: PointerEventPass = PointerEventPass.Main): PointerEvent {
  val maybeConsumedEvent = awaitPointerEvent(pass)
  return maybeConsumedEvent.copy(
    changes = maybeConsumedEvent.changes.filter { !it.isConsumed },
    motionEvent = maybeConsumedEvent.motionEvent,
  )
}

/**
 * Data class to hold the result of a double-tap gesture detection.
 */
private data class DoubleTapResult(
  val tapCount: Int,
  val firstTapTime: Long,
  val detected: Boolean = false,
  val scale: Float = 1f,
  val offset: Offset = Offset.Zero,
)

/**
 * Detects a double-tap gesture.
 * @return A [DoubleTapResult] indicating whether the gesture was consumed and the new state.
 */
private fun handleDoubleTap(
  event: PointerEvent,
  doubleTapThreshold: Int,
  tapCount: Int,
  firstTapTime: Long,
  currentScale: Float,
  doubleTapScale: Float,
  globalPosition: Offset,
  windowSize: IntSize,
): DoubleTapResult {
  if (event.changes.size == 1) {
    val change = event.changes.first()
    if (change.changedToUp()) {
      val currentTime = System.currentTimeMillis()
      if (tapCount == 0) {
        // First tap
        return DoubleTapResult(tapCount = 1, firstTapTime = currentTime)
      } else if (currentTime - firstTapTime < doubleTapThreshold) { // 300ms double-tap threshold
        // Second tap within threshold - double tap detected
        val newScale: Float
        val newOffset: Offset
        if (currentScale > 1f) {
          newScale = 1f
          newOffset = Offset.Zero
        } else {
          newScale = doubleTapScale
          newOffset = Offset(
            y = -change.position.y + globalPosition.y / 2 + windowSize.height / 2 / newScale,
            x = -change.position.x + globalPosition.x / 2 + windowSize.width / 2 / newScale
          )
        }
        return DoubleTapResult(
          tapCount = 0,
          firstTapTime = 0,
          detected = true,
          scale = newScale,
          offset = newOffset
        )
      } else {
        // Second tap but outside time threshold - reset
        return DoubleTapResult(tapCount = 1, firstTapTime = currentTime)
      }
    }
  }
  // Basically do nothing
  return DoubleTapResult(tapCount = tapCount, firstTapTime = firstTapTime)
}

/**
 * If a horizontal drag should be ignored
 */
private fun isHorizontalDrag(scale: Float, offset: Offset, event: PointerEvent): Boolean {
  if (
    scale <= 1f
    &&
    event.changes.size == 1
    &&
    offset.y.absoluteValue <= 10f
  ) {
    val positionChange = event.changes.first().positionChange()
    val aspectRatio = positionChange.x / positionChange.y
    if (!aspectRatio.isNaN() && aspectRatio.absoluteValue > 1f) {
      return true
    }
  }
  return false
}

/**
 * Data class to hold the result of a transformation gesture.
 */
private data class ZoomPanDragResult(
  val scale: Float,
  val offset: Offset,
  val velocityY: Float,
)

/**
 * Handles zoom, pan, and drag transformations.
 * @return A [ZoomPanDragResult] with the new scale, offset, and velocity.
 */
private fun handleZoomPanDrag(
  event: PointerEvent,
  currentScale: Float,
  currentOffset: Offset,
  maxScale: Float,
  maxDragToTop: Float,
  globalPosition: Offset,
): ZoomPanDragResult {
  val zoom = event.calculateZoom()
  val pan = event.calculatePan()
  val centroid = event.calculateCentroid()

  val oldScale = currentScale
  var newScale = currentScale * zoom
  var newOffset = currentOffset
  var newVelocityY = 0f

  if (currentScale > 1f || event.changes.size >= 2) { // Zoom & Pan gesture
    newScale = newScale.coerceIn(1f, maxScale)
    if (newScale > 1f) { // Only allow panning if zoomed in
      newOffset = currentOffset + (pan / newScale)
      if (newScale < maxScale && centroid != Offset.Unspecified) { // Only allow centroid when scale can be changed
        newOffset = newOffset - (centroid / oldScale - centroid / newScale) +
          (globalPosition / oldScale - globalPosition / newScale)
      }
    }
  } else { // Drag gesture
    val newX = currentOffset.x + pan.x
    val yCoefficient = 1f + (currentOffset.y.coerceIn(-maxDragToTop, 0f) / maxDragToTop)
    val newY = currentOffset.y + if (pan.y > 0) pan.y else (pan.y * yCoefficient)
    newOffset = Offset(x = newX, y = newY)
    newVelocityY = pan.y
  }

  return ZoomPanDragResult(scale = newScale, offset = newOffset, velocityY = newVelocityY)
}