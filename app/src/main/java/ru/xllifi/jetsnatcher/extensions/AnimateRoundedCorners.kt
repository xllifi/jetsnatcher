package ru.xllifi.jetsnatcher.extensions

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.xllifi.jetsnatcher.ui.theme.sizes

@Composable
private fun cornerSize(
  animatedVisibilityScope: AnimatedVisibilityScope,
  animationSpec: FiniteAnimationSpec<Dp> = spring(stiffness = Spring.StiffnessMediumLow),
  sizeWhenVisible: Dp = 0.dp,
  sizeWhenHidden: Dp = MaterialTheme.sizes.roundingMedium,
): Dp {
  val cornerSize by animatedVisibilityScope.transition.animateDp(
    transitionSpec = { animationSpec },
    label = "cornerAnimation"
  ) { state ->
    when (state) {
      EnterExitState.Visible -> sizeWhenVisible
      EnterExitState.PreEnter, EnterExitState.PostExit -> sizeWhenHidden
    }
  }

  return cornerSize
}

@Composable
fun Modifier.animateCornerSize(
  animatedVisibilityScope: AnimatedVisibilityScope,
  animationSpec: FiniteAnimationSpec<Dp> = spring(stiffness = Spring.StiffnessMediumLow),
  sizeWhenVisible: Dp = 0.dp,
  sizeWhenHidden: Dp = MaterialTheme.sizes.roundingMedium,
): Modifier {
  val cornerSize = cornerSize(
    animatedVisibilityScope,
    animationSpec,
    sizeWhenVisible,
    sizeWhenHidden
  )
  return clip(RoundedCornerShape(cornerSize))
}

@Composable
fun AnimateCornerSize(
  animatedVisibilityScope: AnimatedVisibilityScope,
  animationSpec: FiniteAnimationSpec<Dp> = tween(300),
  sizeWhenVisible: Dp = 0.dp,
  sizeWhenHidden: Dp = MaterialTheme.sizes.roundingMedium,
  content: @Composable (Dp) -> Unit,
) {
  val cornerSize = cornerSize(
    animatedVisibilityScope,
    animationSpec,
    sizeWhenVisible,
    sizeWhenHidden
  )
  content(cornerSize)
}