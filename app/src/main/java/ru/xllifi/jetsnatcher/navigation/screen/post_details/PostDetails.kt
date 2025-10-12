package ru.xllifi.jetsnatcher.navigation.screen.post_details

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.navigation.screen.browser.BrowserViewModel
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val isUrlRegex = Regex("^(https?://)?.+\\.[a-z]{2,6}(/.*)?$")
val hostnameRegex = Regex("^(?:https?://)?(?:www\\.)?([^/]+)")

@Serializable
data class PostDetailsNavKey(val postId: Int) : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostDetails(
  postId: Int,
  innerPadding: PaddingValues,
) {
  @Composable
  fun Title(
    text: String,
    modifier: Modifier = Modifier,
    isTopPaddingEnabled: Boolean = true,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleMediumEmphasized,
      color = MaterialTheme.colorScheme.primary,
      textAlign = TextAlign.Center,
      modifier = modifier
        .conditional(
          isTopPaddingEnabled,
          Modifier.padding(top = 16.dp)
        ),
    )
  }

  val browserViewModel: BrowserViewModel = viewModel()
  val uiState by browserViewModel.uiState.collectAsState()
  val posts by remember { derivedStateOf { uiState.searches.last().posts } }
  val post by remember { derivedStateOf { posts.first { it.id == postId } } }

  val scrollState = rememberScrollState()
  val selectedTags = remember { mutableStateListOf<String>() }
  val layoutDirection = LocalLayoutDirection.current
  Column(
    modifier = Modifier
      .padding(
        start = innerPadding.calculateStartPadding(layoutDirection),
        end = innerPadding.calculateEndPadding(layoutDirection),
      )
      .padding(horizontal = 16.dp)
      .verticalScroll(state = scrollState),
  ) {
    Box(Modifier.height(innerPadding.calculateTopPadding())) { }
    val tags by remember { derivedStateOf { post.tags ?: post.unparsedTags } }
    var showTags by remember { mutableStateOf(true) }
    Row(
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Title(
        text = "Tags",
        isTopPaddingEnabled = false,
      )
      Box(
        modifier = Modifier
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.primary)
          .padding(4.dp)
          .size(16.dp)
          .clickable { showTags = !showTags }
      ) {
        Icon(
          imageVector = if (showTags) Icons.Outlined.CloseFullscreen else Icons.Outlined.OpenInFull,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimary,
        )
      }
    }
    AnimatedVisibility(
      visible = showTags,
      enter = expandVertically(),
      exit = shrinkVertically(),
    ) {
      FlowRow(
        modifier = Modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        for (tag in tags) {
          Tag(tag) { label, value, fgColor, bgColor ->
            val isSelected = selectedTags.contains(value)
            Box(
              modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(
                  if (isSelected) {
                    bgColor
                  } else {
                    Color.Transparent
                  }
                )
                .border(
                  width = 1.dp,
                  color = bgColor,
                  shape = MaterialTheme.shapes.small
                )
                .height(32.dp)
                .clickable {
                  if (selectedTags.contains(value)) {
                    selectedTags.remove(value)
                  } else {
                    selectedTags.add(value)
                  }
                }
                .padding(horizontal = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                color = if (isSelected) {
                  Color.Black
                } else {
                  fgColor
                },
                style = MaterialTheme.typography.labelLarge
              )
            }
          }
        }
      }
    }

    val isLocationLinkPresent = post.locationLink != null
    val isSourceLinkPresent = post.source != null && post.source!!.matches(isUrlRegex)
    val isMediumImageLinkPresent = post.mediumQualityImage.url.isNotEmpty()
    val isBestImageLinkPresent = post.bestQualityImage.url.isNotEmpty()
    if (isLocationLinkPresent
      || isSourceLinkPresent
      || isMediumImageLinkPresent
      || isBestImageLinkPresent
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Title(text = "Links")
        if (isLocationLinkPresent) {
          val hostname = hostnameRegex.find(post.locationLink!!)?.groupValues?.get(1) ?: "[unknown]"
          LinkButton(
            title = "Location",
            description = "Open $hostname in browser",
            link = post.locationLink!!,
          )
        }
        if (isSourceLinkPresent) {
          val hostname = hostnameRegex.find(post.source!!)?.groupValues?.get(1) ?: "[unknown]"
          LinkButton(
            title = "Source",
            description = "Open $hostname in browser",
            link = post.source!!,
          )
        }
        if (isMediumImageLinkPresent) {
          val hostname =
            hostnameRegex.find(post.mediumQualityImage.url)?.groupValues?.get(1) ?: "[unknown]"
          LinkButton(
            title = "Sample file",
            description = "Open $hostname in browser",
            link = post.mediumQualityImage.url,
          )
        }
        if (isBestImageLinkPresent) {
          val hostname =
            hostnameRegex.find(post.bestQualityImage.url)?.groupValues?.get(1) ?: "[unknown]"
          LinkButton(
            title = "Original file",
            description = "Open $hostname in browser",
            link = post.bestQualityImage.url,
          )
        }
      }
    }

    Box(Modifier.height(innerPadding.calculateBottomPadding())) { }
  }

  AnimatedVisibility(
    visible = selectedTags.isNotEmpty(),
    enter = slideIn(spring()) { IntOffset(x = it.width, y = 0) },
    exit = slideOut(spring()) { IntOffset(x = it.width, y = 0) },
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(32.dp)
    ) {
      var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
      FloatingActionButtonMenu(
        modifier = Modifier.align(Alignment.BottomEnd),
        expanded = fabMenuExpanded,
        button = {
          ToggleFloatingActionButton(
            checked = fabMenuExpanded,
            onCheckedChange = {
              fabMenuExpanded = !fabMenuExpanded
            }
          ) {
            val imageVector by remember {
              derivedStateOf {
                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Tag
              }
            }
            Icon(
              painter = rememberVectorPainter(imageVector),
              contentDescription = null,
              modifier = Modifier.animateIcon({ checkedProgress }),
            )
          }
        },
      ) {
        FloatingActionButtonMenuItem(
          onClick = {
            fabMenuExpanded = false
          },
          text = {
            Text("Add to current search")
          },
          icon = {
            Icon(Icons.Outlined.Add, null)
          },
        )
        FloatingActionButtonMenuItem(
          onClick = {
            fabMenuExpanded = false
          },
          text = {
            Text("New search")
          },
          icon = {
            Icon(Icons.Outlined.Search, null)
          },
        )
        FloatingActionButtonMenuItem(
          onClick = {
            fabMenuExpanded = false
          },
          text = {
            Text("Add to blacklist")
          },
          icon = {
            Icon(Icons.Outlined.Block, null)
          },
        )
        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()
        FloatingActionButtonMenuItem(
          onClick = {
            val text = selectedTags.joinToString(" ")
            fabMenuExpanded = false
            scope.launch {
              clipboard.setClipEntry(
                ClipEntry(
                  clipData = ClipData.newPlainText(text, text)
                )
              )
            }
          },
          text = {
            Text("Copy")
          },
          icon = {
            Icon(Icons.Outlined.ContentCopy, null)
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinkButton(
  title: String,
  description: String,
  link: String,
) {
  val uriHandler = LocalUriHandler.current
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .clip(
          MaterialTheme.shapes.medium.copy(
            bottomEnd = CornerSize(4.dp),
            topEnd = CornerSize(4.dp),
          )
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable { uriHandler.openUri(link) }
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Language,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp),
      )
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMediumEmphasized.copy(lineHeight = 14.sp),
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 14.sp),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .clip(
          MaterialTheme.shapes.medium.copy(
            bottomStart = CornerSize(4.dp),
            topStart = CornerSize(4.dp),
          )
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable {
          scope.launch {
            clipboard.setClipEntry(
              ClipEntry(
                clipData = ClipData.newPlainText(link, link)
              )
            )
          }
        }
        .padding(horizontal = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.ContentCopy,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp),
      )
    }
  }
}

@FullPreview
@Composable
fun LinkButtonPreview() {
  JetSnatcherTheme {
    LinkButton(
      title = "Link Title",
      description = "From google.com",
      link = "https://google.com"
    )
  }
}

@FullPreview
@Composable
fun PostDetailsPreview() {
  JetSnatcherTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      PostDetails(
        postId = 0,
        innerPadding = PaddingValues(0.dp),
      )
    }
  }
}