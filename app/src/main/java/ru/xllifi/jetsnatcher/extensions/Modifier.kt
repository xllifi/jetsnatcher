package ru.xllifi.jetsnatcher.extensions

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)

fun Modifier.conditional(
  condition: Boolean,
  ifTrue: Modifier,
): Modifier {
  return then(
    if (condition) {
      ifTrue
    } else {
      Modifier
    }
  )
}

fun Modifier.conditional(
  condition: Boolean,
  ifTrue: Modifier,
  ifFalse: Modifier,
): Modifier {
  return then(
    if (condition) {
      ifTrue
    } else {
      ifFalse
    }
  )
}