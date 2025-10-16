package ru.xllifi.jetsnatcher.navigation.screen.main.post_view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.xllifi.jetsnatcher.extensions.isHorizontal
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import kotlin.math.absoluteValue

@Composable
fun AllGestures(
  modifier: Modifier = Modifier.Companion,
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
  var offset by remember { mutableStateOf(Offset.Companion.Zero) }
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
  var intSize: IntSize = IntSize.Companion.Zero
  var globalPosition: Offset = Offset.Companion.Zero

  LaunchedEffect(predictiveBackProgress) {
    if (predictiveBackProgress > 0f) {
      scale = 1f
      offset = Offset.Companion.Zero
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
      offset = Offset.Companion.Zero
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
      Modifier.Companion
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
  val offset: Offset = Offset.Companion.Zero,
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
          newOffset = Offset.Companion.Zero
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
      if (newScale < maxScale && centroid != Offset.Companion.Unspecified) { // Only allow centroid when scale can be changed
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