package ru.xllifi.jetsnatcher.ui.forms

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import ru.xllifi.jetsnatcher.extensions.isImeHalfVisible
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlDoubleActionList
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlSlider
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlSwitch
import ru.xllifi.jetsnatcher.ui.generic.controls.ControlTextField
import kotlin.collections.iterator
import kotlin.collections.set

@Serializable
class FormEditScope<K> {
  val map: MutableMap<K, ParamValue<*>> = mutableMapOf()

  data class TextParamValue<T : CharSequence>(
    override val title: String,
    override val description: String?,
    override val value: T
  ) : ParamValue<T>

  fun textParam(
    title: String,
    description: String?,
    key: K,
    value: String,
  ) {
    map[key] = TextParamValue(
      title = title,
      description = description,
      value = value,
    )
  }

  data class SwitchParamValue(
    override val title: String,
    override val description: String?,
    override val value: Boolean
  ) : ParamValue<Boolean>

  fun switchParam(
    title: String,
    description: String?,
    key: K,
    value: Boolean
  ) {
    map[key] = SwitchParamValue(
      title = title,
      description = description,
      value = value,
    )
  }

  data class SliderParamValue(
    override val title: String,
    override val description: String?,
    override val value: Float,
    val steps: Int,
    val range: ClosedFloatingPointRange<Float>,
  ) : ParamValue<Float>

  fun sliderParam(
    title: String,
    description: String?,
    key: K,
    value: Float,
    steps: Int = 0,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
  ) {
    map[key] = SliderParamValue(
      title = title,
      description = description,
      value = value,
      steps = steps,
      range = range,
    )
  }

  data class ListSelectParamValue<T>(
    override val title: String,
    override val description: String?,
    override val value: ListAndSelected<T>,
    val itemIconTransform: (list: List<T>, selected: Int, current: Int) -> ImageVector?,
    val itemTitleTransform: (T) -> String,
    val itemDescriptionTransform: (T) -> String?,
  ) : ParamValue<ListSelectParamValue.ListAndSelected<T>> {
    data class ListAndSelected<T>(
      val list: List<T>,
      val selectedIndex: Int,
    )
  }

  fun <T> listSelectParam(
    title: String,
    description: String?,
    key: K,
    value: List<T>,
    itemIconTransform: (list: List<T>, selected: Int, current: Int) -> ImageVector?,
    itemTitleTransform: (T) -> String,
    itemDescriptionTransform: (T) -> String?,
  ) {
    map[key] = ListSelectParamValue(
      title = title,
      description = description,
      value = ListSelectParamValue.ListAndSelected(
        value,
        0,
      ),
      itemIconTransform = itemIconTransform,
      itemTitleTransform = itemTitleTransform,
      itemDescriptionTransform = itemDescriptionTransform,
    )
  }

  interface ParamValue<T> {
    val title: String
    val description: String?
    val value: T
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
  ExperimentalLayoutApi::class
)
@Composable
fun <K> FormEditPage(
  title: String,
  onBack: () -> Unit,
  onSave: (Map<K, FormEditScope.ParamValue<*>>) -> Unit = {},
  content: FormEditScope<K>.() -> Unit,
) {
  val scope = FormEditScope<K>()
  scope.content()
  val tempMap = remember {
    mutableStateMapOf(
      *scope.map
        .map { it.key to it.value }
        .toTypedArray()
    )
  }
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
              ),
            tooltip = { PlainTooltip { Text("Back") } },
            state = rememberTooltipState(),
          ) {
            IconButton(onClick = onBack) {
              Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
          }
        },
      )
    }
  ) { innerPadding ->
    val imeHalfVisible = WindowInsets.isImeHalfVisible
    val imeVisible = WindowInsets.isImeVisible
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .imePadding()
        .padding(
          PaddingValues(
            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
            top = innerPadding.calculateTopPadding(),
            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
            bottom = if (imeHalfVisible) 0.dp else innerPadding.calculateBottomPadding(),
          )
        )
        .padding(16.dp),
    ) {
      FormEditControlsGroup(
        tempMap = tempMap,
        tempMapAssign = { k, v -> tempMap[k] = v },
      )
      // region buttons
      AnimatedVisibility(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(bottom = 16.dp),
        visible = !imeVisible,
        enter = slideIn { (_, height) -> IntOffset(x = 0, y = height) } + fadeIn(),
        exit = slideOut { (_, height) -> IntOffset(x = 0, y = height) } + fadeOut(),
      ) {
        ButtonGroup(
          overflowIndicator = {},
          horizontalArrangement = Arrangement.spacedBy(
            space = ButtonGroupDefaults.HorizontalArrangement.spacing,
            alignment = Alignment.CenterHorizontally
          ),
        ) {
          val interactionSourcesIsPressedStates = mutableListOf<State<Boolean>>()
          this.customItem(
            buttonGroupContent = {
              val interactionSource = MutableInteractionSource()
              interactionSourcesIsPressedStates.add(interactionSource.collectIsPressedAsState())
              IconButton(
                onClick = onBack,
                interactionSource = interactionSource,
                modifier = Modifier
                  .size(IconButtonDefaults.largeContainerSize(IconButtonDefaults.IconButtonWidthOption.Uniform))
                  .animateWidth(interactionSource),
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.errorContainer,
                  contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                shapes = IconButtonShapes(
                  shape = IconButtonDefaults.largeRoundShape,
                  pressedShape = IconButtonDefaults.largePressedShape,
                )
              ) {
                Icon(
                  Icons.Outlined.Close,
                  null,
                  modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                )
              }
            },
            menuContent = {}
          )
          this.customItem(
            buttonGroupContent = {
              val interactionSource = MutableInteractionSource()
              interactionSourcesIsPressedStates.add(interactionSource.collectIsPressedAsState())
              val isAnyOtherPressed by remember { derivedStateOf { interactionSourcesIsPressedStates.fastAny { it.value } } }
              val thisPressed by interactionSource.collectIsPressedAsState()
              Button(
                interactionSource = interactionSource,
                onClick = { onSave(tempMap) },
                modifier = Modifier
                  .height(ButtonDefaults.LargeContainerHeight)
                  .animateWidth(interactionSource),
                contentPadding = PaddingValues(
                  top = ButtonDefaults.LargeContentPadding.calculateTopPadding(),
                  bottom = ButtonDefaults.LargeContentPadding.calculateBottomPadding(),
                ),
                shapes = ButtonShapes(
                  shape = CircleShape,
                  pressedShape = ButtonDefaults.largePressedShape,
                )
              ) {
                val startPadding =
                  ButtonDefaults.LargeContentPadding.calculateStartPadding(LocalLayoutDirection.current)
                val endPadding =
                  ButtonDefaults.LargeContentPadding.calculateEndPadding(LocalLayoutDirection.current)
                val shrinkRatio by
                animateFloatAsState(
                  if (isAnyOtherPressed) ButtonGroupDefaults.ExpandedRatio
                  else 0f
                )
                val expandRatio by
                animateFloatAsState(
                  if (thisPressed) ButtonGroupDefaults.ExpandedRatio
                  else 0f
                )
                Box(Modifier.width(startPadding * (1f - shrinkRatio) * (1f + expandRatio)))
                Icon(
                  Icons.Outlined.Save,
                  null,
                  modifier = Modifier
                    .size(ButtonDefaults.LargeIconSize),
                )
                Box(Modifier.width(8.dp))
                Text(
                  text = "Save",
                  style = ButtonDefaults
                    .textStyleFor(ButtonDefaults.LargeContainerHeight)
                    .copy(fontWeight = FontWeight.SemiBold),
                )
                Box(Modifier.width(endPadding * (1f + shrinkRatio) * (1f - expandRatio)))
              }
            },
            menuContent = {}
          )
        }
      }
      // endregion
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <K> FormEditControlsGroup(
  tempMap: SnapshotStateMap<K, FormEditScope.ParamValue<*>>,
  tempMapAssign: (K, FormEditScope.ParamValue<*>) -> Unit,
) {
  val imeVisible = WindowInsets.isImeHalfVisible
  val animatedPadding by animateDpAsState(if (imeVisible) 0.dp else ButtonDefaults.LargeContainerHeight + 32.dp)
  LazyColumn(
    modifier = Modifier
      .clip(MaterialTheme.shapes.large),
    verticalArrangement = Arrangement.spacedBy(2.dp),
    contentPadding = PaddingValues(bottom = animatedPadding)
  ) {
    for ((k, v) in tempMap) {
      item {
        val i = tempMap.keys.indexOf(k)
        val shape = if (i == 0) {
          RoundedCornerShape(
            topStart = MaterialTheme.shapes.large.topStart,
            topEnd = MaterialTheme.shapes.large.topEnd,
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
          )
        } else if (i == tempMap.keys.size - 1) {
          RoundedCornerShape(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
            bottomStart = MaterialTheme.shapes.large.bottomStart,
            bottomEnd = MaterialTheme.shapes.large.bottomEnd,
          )
        } else {
          null
        }
        Box(
          if (shape != null) Modifier.clip(shape) else Modifier
        ) {
          when (v) {
            is FormEditScope.TextParamValue -> {
              with(v) {
                ControlTextField(
                  title = title,
                  description = description,
                  value = value.toString(),
                  onValueChange = {
                    tempMapAssign(
                      k, FormEditScope.TextParamValue(
                        title = title,
                        description = description,
                        value = it,
                      )
                    )
                  }
                )
              }
            }

            is FormEditScope.SwitchParamValue -> {
              with(v) {
                ControlSwitch(
                  title = title,
                  description = description,
                  checked = value,
                  onCheckedChange = {
                    tempMapAssign(
                      k, FormEditScope.SwitchParamValue(
                        title = title,
                        description = description,
                        value = it,
                      )
                    )
                  },
                )
              }
            }

            is FormEditScope.SliderParamValue -> {
              with(v) {
                ControlSlider(
                  title = title,
                  description = description,
                  value = value,
                  onValueChange = {
                    tempMapAssign(
                      k, FormEditScope.SliderParamValue(
                        value = it,
                        title = title,
                        description = description,
                        steps = steps,
                        range = range,
                      )
                    )
                  },
                  steps = steps,
                  valueRange = range,
                )
              }
            }

            is FormEditScope.ListSelectParamValue<*> -> {
              with(v) {
                ControlDoubleActionList(
                  title = title,
                  buttonText = null,
                  buttonIcon = null,
                  onButtonClick = {},
                  items = value.list,
                  itemTitleTransform = itemTitleTransform,
                  itemDescriptionTransform = itemDescriptionTransform,
                  itemPrimaryActionIconTransform = {
                    itemIconTransform(
                      value.list,
                      value.selectedIndex,
                      value.list.indexOf(it),
                    )
                  },
                  itemSecondaryActionIconTransform = { null },
                  onItemPrimaryActionClick = {
                    tempMapAssign(
                      k, FormEditScope.ListSelectParamValue(
                        title = title,
                        description = description,
                        value = FormEditScope.ListSelectParamValue.ListAndSelected(
                          list = value.list,
                          selectedIndex = value.list.indexOf(it)
                        ),
                        itemIconTransform = itemIconTransform,
                        itemTitleTransform = itemTitleTransform,
                        itemDescriptionTransform = itemDescriptionTransform
                      )
                    )
                  },
                  onItemSecondaryActionClick = { },
                )
              }
            }
          }
        }
      }
    }
  }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@FullPreview
fun FormEditPagePreview() {
  PreviewSetup {
    Scaffold {
      FormEditPage(
        title = "Form edit",
        onBack = { },
      ) {
        textParam(
          title = "name title",
          description = "name description",
          key = "name",
          value = "InitValue",
        )
        switchParam(
          title = "switch title",
          description = "switch description",
          key = "switch",
          value = false,
        )
        sliderParam(
          title = "slider title",
          description = "slider description",
          key = "slider",
          value = 1f,
        )
        listSelectParam(
          title = "list select title",
          description = "list select description",
          key = "list_select",
          value = listOf(
            "item1",
            "item2",
            "item3",
          ),
          itemIconTransform = { _, selected, current ->
            if (current == selected) {
              Icons.Default.RadioButtonChecked
            } else {
              Icons.Default.RadioButtonUnchecked
            }
          },
          itemTitleTransform = { it },
          itemDescriptionTransform = { "$it's description" },
        )
      }
    }
  }
}

