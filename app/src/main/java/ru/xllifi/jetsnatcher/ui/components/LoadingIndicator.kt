package ru.xllifi.jetsnatcher.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Elevation
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorMaxDistance
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicatorElevation
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.indicatorShape
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicator(
  state: PullToRefreshState,
  isRefreshing: Boolean,
  modifier: Modifier = Modifier,
  containerColor: Color = PullToRefreshDefaults.loadingIndicatorContainerColor,
  color: Color = PullToRefreshDefaults.loadingIndicatorColor,
  elevation: Dp = LoadingIndicatorElevation,
  minDistance: Dp = 0.dp,
  maxDistance: Dp = IndicatorMaxDistance,
) {
  IndicatorBox(
    modifier = modifier
      .size(
        width = LoadingIndicatorDefaults.ContainerWidth,
        height = LoadingIndicatorDefaults.ContainerHeight
      ),
    state = state,
    isRefreshing = isRefreshing,
    containerColor = containerColor,
    elevation = elevation,
    minDistance = minDistance,
    maxDistance = maxDistance,
  ) {
    Crossfade(
      targetState = isRefreshing,
      animationSpec = spring(),
    ) { refreshing ->
      if (refreshing) {
        ContainedLoadingIndicator(
          modifier =
            Modifier
              .requiredSize(
                width = LoadingIndicatorDefaults.ContainerWidth,
                height = LoadingIndicatorDefaults.ContainerHeight,
              ),
          containerColor = containerColor,
          indicatorColor = color,
        )
      } else {
        // The LoadingIndicator will rotate and morph for a coerced progress value of 0
        // to 1. When the state's distanceFraction is above one, we rotate the entire
        // component we have a continuous rotation until the refreshing flag is true.
        ContainedLoadingIndicator(
          progress = { state.distanceFraction },
          modifier =
            Modifier
              .requiredSize(
                width = LoadingIndicatorDefaults.ContainerWidth,
                height = LoadingIndicatorDefaults.ContainerHeight,
              )
              .drawWithContent {
                val progress = state.distanceFraction
                if (progress > 1f) {
                  // Start the rotation on progress - 1 (i.e. 0) to avoid a
                  // jump that would be more noticeable on some
                  // LoadingIndicator shapes.
                  rotate(-(progress - 1) * 180) {
                    this@drawWithContent.drawContent()
                  }
                } else {
                  drawContent()
                }
              },
          containerColor = containerColor,
          indicatorColor = color,
        )
      }
    }
  }
}
@Composable
fun IndicatorBox(
  state: PullToRefreshState,
  isRefreshing: Boolean,
  modifier: Modifier = Modifier,
  minDistance: Dp = 0.dp,
  maxDistance: Dp = IndicatorMaxDistance,
  shape: Shape = indicatorShape,
  containerColor: Color = Color.Unspecified,
  elevation: Dp = Elevation,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier =
      modifier
        .size(40.dp)
        .drawWithContent {
          clipRect(
            top = 0f,
            left = -Float.MAX_VALUE,
            right = Float.MAX_VALUE,
            bottom = Float.MAX_VALUE,
          ) {
            this@drawWithContent.drawContent()
          }
        }
        .layout { measurable, constraints ->
          val placeable = measurable.measure(constraints)
          layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(
              0,
              0,
              layerBlock = {
                val showElevation = state.distanceFraction > 0f || isRefreshing
                translationY = state.distanceFraction * minDistance.roundToPx() +
                  state.distanceFraction * maxDistance.roundToPx() -
                      size.height
                shadowElevation = if (showElevation) elevation.toPx() else 0f
                this.shape = shape
                clip = true
              },
            )
          }
        }
        .background(color = containerColor, shape = shape),
    contentAlignment = Alignment.Center,
    content = content,
  )
}