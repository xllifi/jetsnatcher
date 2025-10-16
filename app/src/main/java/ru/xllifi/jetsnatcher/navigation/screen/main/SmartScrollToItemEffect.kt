package ru.xllifi.jetsnatcher.navigation.screen.main

import android.util.Log
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SmartScrollToItemEffect(
  gridState: LazyStaggeredGridState,
  index: Int,
  postExpanded: Boolean,
) {
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(index) { // Re-evaluate when index changes
    if (!postExpanded) return@LaunchedEffect // Otherwise user will see rough animation
    if (index < 0) return@LaunchedEffect // Invalid index

    val layoutInfo = gridState.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    if (visibleItemsInfo.isEmpty()) {
      Log.i("SmartScrollToItem", "No items are visible yet")
      // If no items are visible yet, scroll to the target index
      coroutineScope.launch {
        gridState.scrollToItem(index)
      }
      return@LaunchedEffect
    }

    val targetItemInfo = visibleItemsInfo.find { it.index == index }

    if (targetItemInfo == null) {
      Log.i("SmartScrollToItem", "Item is not visible at all")
      // Not visible at all
      gridState.scrollToItem(index)
    } else {
      // Item is visible, check if it's *fully* visible.
      val viewportHeight = layoutInfo.viewportSize.height
      val itemTop = targetItemInfo.offset.y
      val isPartiallyVisibleAtTop = itemTop < 0
      if (isPartiallyVisibleAtTop) {
        Log.i("SmartScrollToItem", "Item is partially visible at top")
        coroutineScope.launch {
          gridState.scrollToItem(index)
        }
        return@LaunchedEffect
      }

      val itemBottom = targetItemInfo.offset.y + targetItemInfo.size.height
      val beforeContentPadding = layoutInfo.beforeContentPadding
      val isPartiallyVisibleAtBottom = itemBottom + beforeContentPadding > viewportHeight
      if (isPartiallyVisibleAtBottom) {
        Log.i("SmartScrollToItem", "Item is partially visible at bottom")
        coroutineScope.launch {
          val scrollOffset =
            -viewportHeight + targetItemInfo.size.height + beforeContentPadding + layoutInfo.afterContentPadding
          gridState.scrollToItem(index, scrollOffset)
        }
        return@LaunchedEffect
      }
    }
    Log.i("SmartScrollToItem", "Item visible")
  }
}