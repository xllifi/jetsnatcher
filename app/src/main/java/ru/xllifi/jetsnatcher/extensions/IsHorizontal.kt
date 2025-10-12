package ru.xllifi.jetsnatcher.extensions

import androidx.compose.ui.unit.IntSize

fun IntSize.isHorizontal(): Boolean {
  if (this.height == 0) {
    return true
  }
  return this.width / this.height > 1
}