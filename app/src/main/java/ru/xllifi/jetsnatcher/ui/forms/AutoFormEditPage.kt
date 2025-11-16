package ru.xllifi.jetsnatcher.ui.forms

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.PreviewSetup
import java.util.Locale
import kotlin.collections.iterator

@Serializable
data class AutoFormEditPageNavKey<K>(
  val title: String,
  val onSave: (Map<K, FormEditScope.ParamValue<*>>) -> Unit = {},
  @Contextual val map: Map<K, Any>,
  @Contextual val customTransforms: (FormEditScope<K>.(key: K, value: Any) -> Unit)? = null,
) : NavKey

@Composable
fun <K> AutoFormEditPage(
  title: String,
  onBack: () -> Unit,
  onSave: (Map<K, FormEditScope.ParamValue<*>>) -> Unit = {},
  map: Map<K, Any>,
  customTransforms: (FormEditScope<K>.(key: K, value: Any) -> Unit)? = null,
) {
  fun String.toHumanReadable(): String =
    this.replace(Regex("[_-]"), " ")
      .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
  FormEditPage(
    title = title,
    onBack = onBack,
    onSave = onSave,
  ) {
    for ((k, v) in map) {
      when (v) {
        is CharSequence -> {
          textParam(
            title = k.toString().toHumanReadable(),
            description = null,
            key = k,
            value = v.toString(),
          )
        }

        is Number -> {
          val v = v.toFloat()
          sliderParam(
            title = k.toString().toHumanReadable(),
            description = null,
            key = k,
            value = v,
            range = v - v..v + v
          )
        }

        is ClosedFloatingPointRange<*> -> {
          if (v.start !is Number) {
            throw IllegalArgumentException("Only numeric ClosedFloatingPointRanges are supported")
          }
          val start = (v.start as Number).toFloat()
          val end = (v.endInclusive as Number).toFloat()
          sliderParam(
            title = k.toString().toHumanReadable(),
            description = null,
            key = k,
            value = start,
            range = start..end,
          )
        }

        is Boolean -> {
          switchParam(
            title = k.toString().toHumanReadable(),
            description = null,
            key = k,
            value = v,
          )
        }

        is Collection<*> -> {
          listSelectParam(
            title = k.toString().toHumanReadable(),
            description = null,
            key = k,
            value = v.toList(),
            itemIconTransform = { _, selected, current ->
              if (current == selected) {
                Icons.Default.RadioButtonChecked
              } else {
                Icons.Default.RadioButtonUnchecked
              }
            },
            itemTitleTransform = { it.toString().toHumanReadable() },
            itemDescriptionTransform = { null },
          )
        }

        else -> {
          if (customTransforms == null) {
            throw IllegalArgumentException(
              "Class ${v::class.simpleName ?: v::class.java.name} is not supported by AutoFormEditPage! " +
                  "Supply a customTransforms lambda, use a supported class or create the page manually with FormEditPage."
            )
          } else {
            customTransforms(k, v)
          }
        }
      }
    }
  }
}

@Composable
@FullPreview
fun AutoFormEditPagePreview() {
  PreviewSetup {
    AutoFormEditPage(
      title = "Form edit",
      onBack = { },
      onSave = {
        Log.d("NAV_ROOT_MAP_EDIT", it.toString())
      },
      map = mapOf(
        "first_name" to "John",
        "last_name" to "Doe",
        "is_registered" to false,
        "age" to 18f..99f,
        "favorite_food" to listOf(
          "pancakes",
          "waffles",
          "hot dogs",
          "french fries",
          "burgers",
          "ice cream",
        )
      ),
    )
  }
}