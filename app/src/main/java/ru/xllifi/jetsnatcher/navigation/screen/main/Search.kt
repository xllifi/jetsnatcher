package ru.xllifi.jetsnatcher.navigation.screen.main

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.components.Tag
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun Search(
  providerIndex: Int,
  searchTags: List<Tag>,
  innerPadding: PaddingValues,
  onNewSearch: (providerIndex: Int, searchTags: List<Tag>) -> Unit,
) {
  val mutableTags = remember { searchTags.toMutableStateList() }
  var autocompleteTags by remember { mutableStateOf(emptyList<Tag>()) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
  ) {
    var value by remember { mutableStateOf("") }
    val layoutDirection = LocalLayoutDirection.current
    val isImeVisible = WindowInsets.isImeVisible
    Column(
      modifier = Modifier
        .padding(
          PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            top = innerPadding.calculateTopPadding(),
            bottom = if (isImeVisible) {
              0.dp
            } else {
              innerPadding.calculateBottomPadding()
            },
          )
        )
        .padding(horizontal = 16.dp),
    ) {
      TextField(
        value = value,
        onValueChange = { value = it },
        onDone = {
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
        }
      )

      AnimatedVisibility(
        visible = value.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        SearchTagsFlowRow(
          modifier = Modifier
            .padding(top = 12.dp),
          tags = mutableTags,
          onInverseTag = { index, newVal -> mutableTags[index] = newVal },
          onRemoveTag = { mutableTags.remove(it) }
        )
      }
      AnimatedVisibility(
        visible = value.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        // TODO: uncomment and fix
        AutocompleteTags(
          providerIndex = providerIndex,
          modifier = Modifier.padding(vertical = 12.dp),
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

    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
      visible = value.isEmpty(),
      enter = slideIn { IntOffset(x = it.width, y = 0) } + fadeIn(),
      exit = slideOut { IntOffset(x = it.width, y = 0) } + fadeOut(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        SearchFab {
          keyboardController?.hide()
          onNewSearch(providerIndex, mutableTags)
        }
      }
    }
  }
}

// TODO: uncomment and fix
//@FullPreview
//@Composable
//fun SearchPreview() {
//  JetSnatcherTheme {
//    Box(
//      modifier = Modifier
//        .fillMaxSize()
//        .background(MaterialTheme.colorScheme.background)
//    ) {
//      Search(
//        searchData = SearchData(providerIndex = 0),
//        innerPadding = PaddingValues(0.dp),
//        onNewSearch = {}
//      )
//    }
//  }
//}

@Composable
private fun TextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (newVal: String) -> Unit,
  onDone: (value: String) -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  BasicTextField(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.small)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .focusRequester(focusRequester),
    value = value,
    onValueChange = { onValueChange(it) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions { onDone(value) },
    textStyle = MaterialTheme.typography.bodyLarge.copy(
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 16.sp,
      lineHeight = 16.sp,
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
        )
        Box {
          if (value.isEmpty()) {
            Text(
              text = "Search tags...", // TODO: translate
              style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                fontSize = 16.sp,
                lineHeight = 16.sp,
              )
            )
          }
          innerTextField()
        }
      }
    }
  )
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
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

@Composable
fun AutocompleteTags(
  modifier: Modifier = Modifier,
  providerIndex: Int,
  tagPart: String,
  autocompleteTags: List<Tag>,
  onNewAutocompleteTags: (newAc: List<Tag>) -> Unit,
  onTagClick: (tag: Tag) -> Unit,
) {
  val context = LocalContext.current
  LaunchedEffect(tagPart) {
    val settings = context.settingsDataStore.data.first()
    val provider = settings.getProvider(providerIndex).toReal()
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
        index = index,
        firstIndex = 0,
        lastIndex = autocompleteTags.lastIndex,
        roundedCornerSize = MaterialTheme.shapes.medium.topStart,
        sharpCornerSize = CornerSize(4.dp),
        tag = tag,
        onTagClick = onTagClick
      )
    }
  }
}

@Composable
fun LazyItemScope.AutocompleteTag(
  index: Int,
  firstIndex: Int,
  lastIndex: Int,
  roundedCornerSize: CornerSize,
  sharpCornerSize: CornerSize,
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
        .clip(
          RoundedCornerShape(
            topStart = if (index == firstIndex) roundedCornerSize else sharpCornerSize,
            topEnd = if (index == firstIndex) roundedCornerSize else sharpCornerSize,
            bottomEnd = if (index == lastIndex) roundedCornerSize else sharpCornerSize,
            bottomStart = if (index == lastIndex) roundedCornerSize else sharpCornerSize,
          )
        )
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
      providerIndex = 0,
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.SearchFab(
  onClick: () -> Unit,
) {
  FloatingActionButton(
    onClick = onClick,
    modifier = Modifier
      .align(Alignment.BottomEnd)
      .padding(32.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 20.dp),
    ) {
      Text(
        text = "Submit",
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