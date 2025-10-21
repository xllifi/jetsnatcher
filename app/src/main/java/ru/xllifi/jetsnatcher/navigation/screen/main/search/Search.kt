package ru.xllifi.jetsnatcher.navigation.screen.main.search

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.dpToPx
import ru.xllifi.jetsnatcher.extensions.isImeHalfVisible
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.history.HistoryEntryProto
import ru.xllifi.jetsnatcher.proto.historyDataStore
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.components.TextField
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme
import kotlin.collections.addAll

fun timestampToRelativeTimeSpan(timestamp: Long): String {
  return DateUtils.getRelativeTimeSpanString(
    timestamp,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE
  ).toString()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun Search(
  providerProto: ProviderProto,
  searchTags: List<Tag>,
  innerPadding: PaddingValues,
  onNewSearch: (providerProto: ProviderProto, searchTags: List<Tag>) -> Unit,
) {
  var mutableTags = remember { searchTags.toMutableStateList() }
  var autocompleteTags by remember { mutableStateOf(emptyList<Tag>()) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
  ) {
    var value by remember { mutableStateOf("") }
    val layoutDirection = LocalLayoutDirection.current
    val isImeHalfVisible = WindowInsets.isImeHalfVisible

    val context = LocalContext.current
    val history by context.historyDataStore.data.collectAsState(runBlocking { context.historyDataStore.data.first() })
    var onlyShowFavorite by remember { mutableStateOf(false) }

    Column(
      modifier = Modifier
        .padding(
          PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            top = innerPadding.calculateTopPadding(),
            bottom = if (isImeHalfVisible) {
              0.dp
            } else {
              innerPadding.calculateBottomPadding()
            },
          )
        )
        .padding(horizontal = 16.dp),
    ) {
      val focusRequester = remember { FocusRequester() }
      TextField(
        modifier = Modifier
          .focusRequester(focusRequester),
        value = value,
        onValueChange = { value = it },
        onKeyboardDone = {
          val acTag = autocompleteTags.firstOrNull { it.value == value || it.label == value }
          if (acTag != null) {
            mutableTags.add(acTag)
          } else {
            mutableTags.add(
              Tag(
                label = value,
                value = value,
                postCount = -1,
                category = TagCategory.Unknown
              )
            )
          }
          value = ""
        },
        icon = Icons.Outlined.Search,
      )
      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }

      Box(
        modifier = Modifier
          .padding(top = 12.dp),
      ) {
        this@Column.AnimatedVisibility(
          visible = value.isEmpty(),
          enter = fadeIn(),
          exit = fadeOut(),
        )
        {
          if (mutableTags.isNotEmpty()) {
            SearchTagsFlowRow(
              modifier = Modifier
                .padding(top = 12.dp),
              tags = mutableTags,
              onInverseTag = { index, newVal -> mutableTags[index] = newVal },
              onRemoveTag = { mutableTags.remove(it) }
            )
          } else {
            this@Column.AnimatedVisibility(
              visible = !onlyShowFavorite,
              enter = slideIn { IntOffset(x = -it.width, y = 0) } + fadeIn(),
              exit = slideOut { IntOffset(x = it.width, y = 0) } + fadeOut(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
              ) {
                Text(
                  text = "Recent searches",
                  modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
                LazyColumn(
                  modifier = Modifier
                    .clip(MaterialTheme.shapes.medium),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  items(history.entries.reversed()) { historyEntry ->
                    HistoryEntry(
                      historyEntry = historyEntry,
                      onNewSearch = {
                        mutableTags.removeAll { true }
                        mutableTags.addAll(historyEntry.tags)
                      },
                      onToggleFavorite = {
                        GlobalScope.launch {
                          context.historyDataStore.updateData {
                            val entries = it.entries.toMutableList()
                            val index = entries.indexOf(historyEntry)
                            if (index == -1) return@updateData it
                            entries[index] = historyEntry.copy(
                              isFavorite = !historyEntry.isFavorite
                            )
                            it.copy(
                              entries = entries
                            )
                          }
                        }
                      }
                    )
                  }
                }
              }
            }
            this@Column.AnimatedVisibility(
              visible = onlyShowFavorite,
              enter = slideIn { IntOffset(x = -it.width, y = 0) } + fadeIn(),
              exit = slideOut { IntOffset(x = it.width, y = 0) } + fadeOut(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
              ) {
                Text(
                  text = "Favorite searches",
                  modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
                LazyColumn(
                  modifier = Modifier
                    .clip(MaterialTheme.shapes.medium),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  items(history.entries.filter { it.isFavorite }.reversed()) { historyEntry ->
                    HistoryEntry(
                      historyEntry = historyEntry,
                      onNewSearch = {
                        mutableTags.removeAll { true }
                        mutableTags.addAll(historyEntry.tags)
                      },
                      onToggleFavorite = {
                        GlobalScope.launch {
                          context.historyDataStore.updateData {
                            val entries = it.entries.toMutableList()
                            val index = entries.indexOf(historyEntry)
                            if (index == -1) return@updateData it
                            entries[index] = historyEntry.copy(
                              isFavorite = !historyEntry.isFavorite
                            )
                            it.copy(
                              entries = entries
                            )
                          }
                        }
                      }
                    )
                  }
                }
              }
            }
          }
        }
        this@Column.AnimatedVisibility(
          visible = value.isNotEmpty(),
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          AutocompleteTags(
            providerProto = providerProto,
            modifier = Modifier.padding(bottom = 12.dp),
            tagPart = value,
            autocompleteTags = autocompleteTags,
            onNewAutocompleteTags = { autocompleteTags = it },
            onTagClick = { tag ->
              value = ""
              autocompleteTags = emptyList()
              mutableTags.add(tag)
            }
          )
        }
      }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
      visible = value.isEmpty() && mutableTags.isNotEmpty(),
      enter = slideIn { IntOffset(x = it.width, y = 0) } + fadeIn(),
      exit = slideOut { IntOffset(x = 0, y = it.height) } + fadeOut(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        SearchFab {
          keyboardController?.hide()
          onNewSearch(providerProto, mutableTags)
        }
      }
    }
    AnimatedVisibility(
      visible = value.isEmpty() && mutableTags.isEmpty(),
      enter = slideIn { IntOffset(x = it.width, y = 0) } + fadeIn(),
      exit = slideOut { IntOffset(x = 0, y = it.height) } + fadeOut(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        HistoryFabMenu(
          onFavoriteSearchesClick = { onlyShowFavorite = true },
          onRecentSearchesClick = { onlyShowFavorite = false },
        )
      }
    }
  }
}

@FullPreview
@Composable
fun SearchPreview() {
  JetSnatcherTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      Search(
        innerPadding = PaddingValues(0.dp),
        providerProto = ProviderProto(),
        searchTags = listOf(),
        onNewSearch = { _, _ -> },
      )
    }
  }
}

@Composable
fun SearchTagsFlowRow(
  modifier: Modifier = Modifier,
  tags: List<Tag>,
  onInverseTag: (index: Int, newTag: Tag) -> Unit,
  onRemoveTag: (tag: Tag) -> Unit,
) {
  FlowRow(
    modifier = modifier.padding(bottom = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    for (tag in tags) {
      Tag(
        tag = tag,
        baseFgColor = MaterialTheme.colorScheme.primary,
      ) { label, value, color, _ ->
        Row(
          modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .border(
              width = 1.dp,
              color = color,
              shape = MaterialTheme.shapes.small,
            )
            .height(32.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = label,
            color = Color.Black,
            style = MaterialTheme.typography.labelLarge.copy(
              lineHeight = 32.sp
            ),
            modifier = Modifier
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) {
                val newTag = if (tag.value.startsWith('-')) {
                  tag.copy(
                    label = tag.label.removeRange(0, 1),
                    value = tag.value.removeRange(0, 1)
                  )
                } else {
                  tag.copy(
                    label = "-${tag.label}",
                    value = "-${tag.value}"
                  )
                }
                onInverseTag(tags.indexOf(tag), newTag)
              }
              .padding(start = 16.dp, end = 4.dp)
          )
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
              .size(32.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) {
                onRemoveTag(tag)
              }
              .padding(start = 4.dp, end = 8.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryEntry(
  historyEntry: HistoryEntryProto,
  onNewSearch: (tags: List<Tag>) -> Unit,
  onToggleFavorite: (historyEntry: HistoryEntryProto) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .height(64.dp)
        .clip(MaterialTheme.shapes.extraSmall)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable { onNewSearch(historyEntry.tags) }
        .padding(12.dp)
        .padding(top = 8.dp),
    )
    {
      LazyRow(
        modifier = Modifier
          .clip(MaterialTheme.shapes.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        items(historyEntry.tags) { tag ->
          Tag(
            tag = tag,
            baseBgColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            baseFgColor = MaterialTheme.colorScheme.primary,
          ) { _, _, fgColor, bgColor ->
            Text(
              text = tag.label,
              style = MaterialTheme.typography.bodyLarge,
              color = fgColor,
              modifier = Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(bgColor)
                .padding(8.dp, 6.dp),
            )
          }
        }
      }
      Text(
        text = timestampToRelativeTimeSpan(historyEntry.createdAt),
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
        ),
        modifier = Modifier
          .align(Alignment.TopStart)
          .offset(y = -18.dp)
      )
    }
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(MaterialTheme.shapes.extraSmall)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable { onToggleFavorite(historyEntry) },
      contentAlignment = Alignment.Center,
    ) {
      val progress by animateFloatAsState(if (historyEntry.isFavorite) 1f else 0f)
      val imageVector by remember {
        derivedStateOf {
          if (progress > 0.5f) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
        }
      }
      val colorScheme = MaterialTheme.colorScheme
      Icon(
        painter = rememberVectorPainter(imageVector),
        contentDescription = null,
        tint = if (historyEntry.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .animateIcon(
            checkedProgress = { progress },
            color = { progress ->
              lerp(colorScheme.onSurface, colorScheme.primary, progress)
            },
            size = { progress ->
              28.dp + (8.dp * progress)
            }
          )
        ,
      )
    }
  }
}

@Composable
fun AutocompleteTags(
  modifier: Modifier = Modifier,
  providerProto: ProviderProto,
  tagPart: String,
  autocompleteTags: List<Tag>,
  onNewAutocompleteTags: (newAc: List<Tag>) -> Unit,
  onTagClick: (tag: Tag) -> Unit,
) {
  val provider by remember { derivedStateOf { providerProto.toReal() } }
  LaunchedEffect(tagPart) {
    onNewAutocompleteTags(provider.getAutoComplete(tagPart.replace(" ", "_")) ?: emptyList())
  }
  LazyColumn(
    modifier = modifier.clip(MaterialTheme.shapes.medium),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    itemsIndexed(
      items = autocompleteTags,
      key = { index, tag -> tag.value }
    ) { index, tag ->
      AutocompleteTag(
        tag = tag,
        onTagClick = onTagClick
      )
    }
  }
}

@Composable
fun LazyItemScope.AutocompleteTag(
  tag: Tag,
  onTagClick: (tag: Tag) -> Unit,
) {
  Tag(
    tag = tag,
    baseBgColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    baseFgColor = MaterialTheme.colorScheme.primary,
  ) { _, _, fgColor, bgColor ->
    Row(
      modifier = Modifier
        .animateItem()
        .clip(MaterialTheme.shapes.extraSmall)
        .fillMaxWidth()
        .background(bgColor)
        .padding(16.dp)
        .clickable { onTagClick(tag) },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = tag.label,
        style = MaterialTheme.typography.bodyLarge,
        color = fgColor,
        modifier = Modifier.weight(1f),
      )
      Text(
        text = "${tag.postCount}",
        style = MaterialTheme.typography.labelMedium.copy(lineHeight = 12.sp),
        color = fgColor.copy(0.4f),
      )
    }
  }
}

@Composable
@FullPreview
fun AutocompleteTagsPreview() {
  JetSnatcherTheme {
    AutocompleteTags(
      tagPart = "hira",
      autocompleteTags = listOf(
        Tag(
          label = "hirasawa yui",
          value = "hirasawa_yui",
          postCount = 12342352,
          category = TagCategory.Character
        ),
        Tag(
          label = "translated",
          value = "translated",
          postCount = 12342352,
          category = TagCategory.Metadata
        ),
        Tag(
          label = "general",
          value = "general",
          postCount = 12342352,
          category = TagCategory.General
        ),
        Tag(
          label = "k-on",
          value = "k-on",
          postCount = 12342352,
          category = TagCategory.Copyright
        ),
        Tag(
          label = "artist",
          value = "artist",
          postCount = 12342352,
          category = TagCategory.Artist
        ),
        Tag(
          label = "deprecated (should never appear here)",
          value = "deprecated",
          postCount = 12342352,
          category = TagCategory.Deprecated
        ),
        Tag(
          label = "unknown (should never appear here)",
          value = "unknown",
          postCount = 12342352,
          category = TagCategory.Unknown
        )
      ),
      onNewAutocompleteTags = {},
      onTagClick = {},
      providerProto = ProviderProto(),
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.SearchFab(
  onClick: () -> Unit,
) {
  val bottomPadding by animateDpAsState(if (WindowInsets.isImeHalfVisible) 0.dp else 64.dp)
  FloatingActionButton(
    onClick = onClick,
    modifier = Modifier
      .align(Alignment.BottomEnd)
      .padding(bottom = bottomPadding)
      .padding(16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 20.dp),
    ) {
      Text(
        text = "Search", // TODO: Translate
        style = MaterialTheme.typography.bodyLargeEmphasized.copy(
          fontWeight = FontWeight.Medium
        )
      )
      Icon(
        imageVector = Icons.Outlined.Search,
        contentDescription = null
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun BoxScope.HistoryFabMenu(
  onFavoriteSearchesClick: () -> Unit,
  onRecentSearchesClick: () -> Unit,
) {
  var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
  val bottomPadding by animateDpAsState(if (WindowInsets.isImeHalfVisible) 0.dp else 64.dp)
  FloatingActionButtonMenu(
    modifier = Modifier
      .align(Alignment.BottomEnd)
      .padding(bottom = bottomPadding),
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
            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.AutoMirrored.Filled.ManageSearch
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
      onClick = onFavoriteSearchesClick,
      text = {
        Text("Favorite Searches") // TODO: Translate
      },
      icon = {
        Icon(Icons.Outlined.FavoriteBorder, null)
      },
    )
    FloatingActionButtonMenuItem(
      onClick = onRecentSearchesClick,
      text = {
        Text("Recent Searches") // TODO: Translate
      },
      icon = {
        Icon(Icons.Outlined.History, null)
      },
    )
  }
}