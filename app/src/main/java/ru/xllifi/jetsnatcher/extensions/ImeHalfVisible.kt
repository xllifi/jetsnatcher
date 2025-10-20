package ru.xllifi.jetsnatcher.extensions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val WindowInsets.Companion.isImeHalfVisible: Boolean
  @Composable
  get() = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 200.dp