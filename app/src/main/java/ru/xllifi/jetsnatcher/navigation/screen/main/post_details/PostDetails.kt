package ru.xllifi.jetsnatcher.navigation.screen.main.post_details

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.extensions.conditional
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModel
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.ui.components.DoubleActionListEntry
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.settings.components.SettingDoubleActionList

val isUrlRegex = Regex("^(https?://)?.+\\.[a-z]{2,6}(/.*)?$")
val hostnameRegex = Regex("^(?:https?://)?(?:www\\.)?([^/]+)")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostDetails(
  postDetailsViewModel: PostDetailsViewModel = viewModel(),
  browserViewModel: BrowserViewModel,
  postIndex: Int,
  innerPadding: PaddingValues,

  onSelectedTagsAddToSearchClick: (tags: List<Tag>) -> Unit,
  onSelectedTagsNewSearchClick: (tags: List<Tag>) -> Unit,
  onSelectedTagsAddToBlacklistClick: (tags: List<Tag>) -> Unit,
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

  val browserUiState by browserViewModel.uiState.collectAsState()
  val posts by remember { derivedStateOf { browserUiState.posts } }
  val post by remember { derivedStateOf { posts[postIndex] } }

  val postDetailsUiState by postDetailsViewModel.uiState.collectAsState()
  val selectedTags = postDetailsUiState.selectedTags
  val scrollState = rememberScrollState()
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
        text = "Tags", // TODO: Translate
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
          Tag(
            tag = tag,
            baseFgColor = MaterialTheme.colorScheme.primary,
          ) { label, value, color, _ ->
            val isSelected = selectedTags.contains(tag)
            Box(
              modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(
                  if (isSelected) {
                    color
                  } else {
                    Color.Transparent
                  }
                )
                .border(
                  width = 1.dp,
                  color = color,
                  shape = MaterialTheme.shapes.small
                )
                .height(32.dp)
                .clickable {
                  val tag = when (tag) {
                    is String -> Tag(
                      value = tag,
                      label = tag.replace('_', ' '),
                      postCount = 0,
                      category = TagCategory.Unknown
                    )

                    is Tag -> tag
                    else -> throw IllegalArgumentException("`tag` is neither `String` or `Tag`, but ${tag::javaClass.name}")
                  }
                  if (selectedTags.contains(tag)) {
                    postDetailsViewModel.removeTag(tag)
                  } else {
                    postDetailsViewModel.addTag(tag)
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
                  color
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
      data class Link(
        val title: String,
        val url: String,
      ) {
        fun getHostname(): String = hostnameRegex.find(post.locationLink!!)?.groupValues?.get(1) ?: "[unknown]"
      }
      val uriHandler = LocalUriHandler.current
      val clipboard = LocalClipboard.current
      val scope = rememberCoroutineScope()
      fun copy(text: String) {
        scope.launch {
          clipboard.setClipEntry(
            ClipEntry(
              clipData = ClipData.newPlainText(text, text)
            )
          )
        }
      }

      val links = mutableListOf<Link>()

      if (isLocationLinkPresent) {
        links.add(Link(
          "Location",
          post.locationLink!!,
        ))
      }
      if (isSourceLinkPresent) {
        links.add(Link(
          "Source",
          post.source!!,
        ))
      }
      if (isMediumImageLinkPresent) {
        links.add(Link(
          "Sample file",
          post.mediumQualityImage.url,
        ))
      }
      if (isBestImageLinkPresent) {
        links.add(Link(
          "Original file",
          post.bestQualityImage.url,
        ))
      }

      Box(
        modifier = Modifier
          .clip(MaterialTheme.shapes.large)
      ) {
        // TODO: design an unique component for links
        SettingDoubleActionList(
          label = "Links",
          buttonText = null,
          buttonIcon = null,
          onButtonClick = {},
          items = links,
          itemTitleTransform = {it.title},
          itemDescriptionTransform = {"Visit ${it.getHostname()} in browser"},
          itemPrimaryActionIcon = Icons.Outlined.Language,
          onItemPrimaryActionClick = { uriHandler.openUri(it.url) },
          itemSecondaryActionIcon = Icons.Outlined.ContentCopy,
          onItemSecondaryActionClick = { copy(it.url) },
        )
      }
    }

    Box(Modifier.height(innerPadding.calculateBottomPadding())) { }
  }

  AnimatedVisibility(
    visible = selectedTags.isNotEmpty(),
    enter = slideIn(spring()) { IntOffset(x = it.width, y = 0) },
    exit = slideOut(spring()) { IntOffset(x = it.width, y = 0) },
  )
  {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
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
            onSelectedTagsAddToSearchClick(selectedTags)
            fabMenuExpanded = false
          },
          text = {
            Text("Add to current search") // TODO: Translate
          },
          icon = {
            Icon(Icons.Outlined.Add, null)
          },
        )
        FloatingActionButtonMenuItem(
          onClick = {
            onSelectedTagsNewSearchClick(selectedTags)
            fabMenuExpanded = false
          },
          text = {
            Text("New search") // TODO: Translate
          },
          icon = {
            Icon(Icons.Outlined.Search, null)
          },
        )
        FloatingActionButtonMenuItem(
          onClick = {
            onSelectedTagsAddToBlacklistClick(selectedTags)
            fabMenuExpanded = false
          },
          text = {
            Text("Add to blacklist") // TODO: Translate
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
            Text("Copy") // TODO: Translate
          },
          icon = {
            Icon(Icons.Outlined.ContentCopy, null)
          },
        )
      }
    }
  }
}

@FullPreview
@Composable
fun PostDetailsPreview() {
  PreviewSetup {
    Scaffold {
      val vm = BrowserViewModel(
        context = LocalContext.current,
        providerProto = ProviderProto(),
        searchTags = listOf(),
        loadPreviewPosts = true,
      )
      PostDetails(
        browserViewModel = vm,
        postIndex = 0,
        innerPadding = PaddingValues(0.dp),
        onSelectedTagsAddToSearchClick = {},
        onSelectedTagsNewSearchClick = {},
        onSelectedTagsAddToBlacklistClick = {},
      )
    }
  }
}