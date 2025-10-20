package ru.xllifi.jetsnatcher.navigation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.Routes
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme
import kotlin.text.ifEmpty

@Composable
private fun TextField(
  value: String,
  onValueChange: (newVal: String) -> Unit,
  placeholder: String,
  name: String,
  onDone: (value: String) -> Unit,
  icon: @Composable () -> Unit = {},
) {
  BasicTextField(
    value = value.ifEmpty { placeholder },
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
      val labelSp = with(LocalDensity.current) {
        12.dp.toSp()
      }
      Text(
        text = name,
        style = MaterialTheme.typography.labelMedium.copy(
          color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
          fontSize = labelSp,
          lineHeight = labelSp,
        ),
        modifier = Modifier
          .zIndex(2f)
          .offset(x = 12.dp, y = 2.dp),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(MaterialTheme.shapes.small)
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .height(48.dp)
          .padding(top = 8.dp)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        icon()
        innerTextField()
      }
    }
  )
}

@Serializable
data class ProviderEditDialogNavKey(
  val provider: ProviderProto? = null,
  val index: Int? = null,
  val providerType: ProviderType,
) : NavKey

@Composable
fun ProviderEditDialog(
  provider: ProviderProto? = null,
  index: Int? = null,
  providerType: ProviderType,
  onSelectProviderType: () -> Unit,
  onDone: (providerProto: ProviderProto, index: Int?) -> Unit,
) {
  var temporaryProvider by remember {
    mutableStateOf(
      provider?.toTemporary() ?: TemporaryProvider()
    )
  }
  LaunchedEffect(provider) {
    temporaryProvider = provider?.toTemporary() ?: TemporaryProvider()
  }

  var providerDefaultRoutes by remember { mutableStateOf(providerType.getDefaultRoutes()) }
  LaunchedEffect(providerType) {
    providerDefaultRoutes = providerType.getDefaultRoutes()
  }

  Column(
    modifier = Modifier
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    TextField(
      value = temporaryProvider.name,
      onValueChange = { temporaryProvider = temporaryProvider.copy(name = it) },
      name = "Name",
      placeholder = providerType.name,
      onDone = { temporaryProvider = temporaryProvider.copy(name = it) },
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .height(40.dp)
        .clickable { onSelectProviderType() }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      Text(
        text = "Provider type: ${providerType.getFormattedName()}",
        style = MaterialTheme.typography.bodyLarge.copy(
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 16.sp,
          lineHeight = 16.sp,
        )
      )
    }
    TextField(
      value = temporaryProvider.routesBase,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesBase = it) },
      name = "Base URL",
      placeholder = providerDefaultRoutes.base,
      onDone = { temporaryProvider = temporaryProvider.copy(routesBase = it) },
    )
    TextField(
      value = temporaryProvider.routesAutocomplete,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesAutocomplete = it) },
      name = "Autocomplete route",
      placeholder = providerDefaultRoutes.autocomplete,
      onDone = { temporaryProvider = temporaryProvider.copy(routesAutocomplete = it) },
    )
    TextField(
      value = temporaryProvider.routesPosts,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesPosts = it) },
      name = "Posts route",
      placeholder = providerDefaultRoutes.posts,
      onDone = { temporaryProvider = temporaryProvider.copy(routesPosts = it) },
    )
    TextField(
      value = temporaryProvider.routesTags,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesTags = it) },
      name = "Tags route",
      placeholder = providerDefaultRoutes.tags,
      onDone = { temporaryProvider = temporaryProvider.copy(routesTags = it) },
    )
    TextField(
      value = temporaryProvider.routesComments,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesComments = it) },
      name = "Comments route",
      placeholder = providerDefaultRoutes.comments,
      onDone = { temporaryProvider = temporaryProvider.copy(routesComments = it) },
    )
    TextField(
      value = temporaryProvider.routesNotes,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesNotes = it) },
      name = "Notes route",
      placeholder = providerDefaultRoutes.notes,
      onDone = { temporaryProvider = temporaryProvider.copy(routesNotes = it) },
    )
    TextField(
      value = temporaryProvider.routesAuth,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesAuth = it) },
      name = "API key",
      placeholder = "",
      onDone = { temporaryProvider = temporaryProvider.copy(routesAuth = it) },
    )
    Button(
      onClick = {
        val provider = temporaryProvider.toProto(providerType, providerDefaultRoutes)
        onDone(provider, index)
      }
    ) {
      Text("Done")
    }
  }
}

data class TemporaryProvider(
  val name: String = "",
  val routesBase: String = "",
  val routesPublicFacingPostPage: String = "",
  val routesAutocomplete: String = "",
  val routesPosts: String = "",
  val routesTags: String = "",
  val routesComments: String = "",
  val routesNotes: String = "",
  val routesAuth: String = "",
) {
  fun toProto(providerType: ProviderType, defaultRoutes: Routes): ProviderProto {
    return ProviderProto(
      name = this.name.ifEmpty { providerType.name },
      providerType = providerType,
      routes = Routes(
        base = this.routesBase.ifEmpty { defaultRoutes.base },
        publicFacingPostPage = this.routesPublicFacingPostPage.ifEmpty { defaultRoutes.publicFacingPostPage },
        autocomplete = this.routesAutocomplete.ifEmpty { defaultRoutes.autocomplete },
        posts = this.routesPosts.ifEmpty { defaultRoutes.posts },
        tags = this.routesTags.ifEmpty { defaultRoutes.tags },
        comments = this.routesComments.ifEmpty { defaultRoutes.comments },
        notes = this.routesNotes.ifEmpty { defaultRoutes.notes },
        authSuffix = this.routesAuth.ifEmpty { defaultRoutes.authSuffix },
      )
    )
  }
}
fun ProviderProto.toTemporary(): TemporaryProvider {
  return TemporaryProvider(
    name = this.name,
    routesBase = this.routes.base,
    routesPublicFacingPostPage = this.routes.publicFacingPostPage,
    routesAutocomplete = this.routes.autocomplete,
    routesPosts = this.routes.posts,
    routesTags = this.routes.tags,
    routesComments = this.routes.comments,
    routesNotes = this.routes.notes,
    routesAuth = this.routes.authSuffix ?: "",
  )
}

@Composable
@FullPreview
private fun ProviderEditDialogContentPreview() {
  JetSnatcherTheme {
    var providerType by remember { mutableStateOf(ProviderType.Gelbooru) }
    ProviderEditDialog(
      providerType = providerType,
      onSelectProviderType = { /* Show [ProviderTypeDialog] */ },
      onDone = { _, _ -> },
    )
  }
}


@Serializable
data class ProviderTypeDialogNavKey(
  val onSelectType: (providerType: ProviderType) -> Unit,
) : NavKey


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderTypeDialog(
  onSelectType: (providerType: ProviderType) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .verticalScroll(rememberScrollState())
      .padding(12.dp),
  ) {
    Text(
      text = "Select provider type",
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(bottom = 16.dp),
    )
    LazyColumn(
      modifier = Modifier
        .heightIn(max = 200.dp)
        .clip(MaterialTheme.shapes.small),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(ProviderType.entries) { providerType ->
        Text(
          text = providerType.getFormattedName(),
          color = MaterialTheme.colorScheme.onPrimary,
          style = MaterialTheme.typography.labelLargeEmphasized,
          modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onSelectType(providerType) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
@FullPreview
private fun ProviderTypeDialogPreview() {
  JetSnatcherTheme {
    ProviderTypeDialog(
      onSelectType = {}
    )
  }
}