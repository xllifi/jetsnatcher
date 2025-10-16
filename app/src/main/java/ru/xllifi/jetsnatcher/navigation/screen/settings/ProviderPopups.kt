package ru.xllifi.jetsnatcher.navigation.screen.settings

import android.util.Log
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import ru.xllifi.booru_api.Providers
import ru.xllifi.booru_api.Routes
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.extensions.toProto
import ru.xllifi.jetsnatcher.proto.Provider
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

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

@Composable
fun ProviderInfoDialog(
  provider: Provider? = null,
  onDone: (newProvider: Provider) -> Unit,
  onDismissRequest: () -> Unit,
  providerType: Providers,
  /** This should show [ProviderTypeDialog] */
  onSelectProviderType: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
  ) {
    ProviderInfoDialogContent(
      provider = provider,
      onDone = onDone,
      providerType = providerType,
      onSelectProviderType = onSelectProviderType,
    )
  }
}

@Composable
fun ProviderInfoDialogContent(
  provider: Provider? = null,
  onDone: (newProvider: Provider) -> Unit,
  providerType: Providers,
  onSelectProviderType: () -> Unit,
) {
  var temporaryProvider by remember { mutableStateOf(provider?.toTemporary() ?: TemporaryProvider()) }
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
      value = temporaryProvider.routesAuth,
      onValueChange = { temporaryProvider = temporaryProvider.copy(routesAuth = it) },
      name = "API key",
      placeholder = "",
      onDone = { temporaryProvider = temporaryProvider.copy(routesAuth = it) },
    )
    Button(
      onClick = {
        val type: Provider.ProviderType = providerType.toProto()
        val newProvider = Provider.newBuilder()
          .setName(temporaryProvider.name.ifEmpty { providerType.name })
          .setProviderType(type)
          .setRoutes(
            Provider.Routes.newBuilder()
              .setBase(temporaryProvider.routesBase.ifEmpty { providerDefaultRoutes.base })
              .setAutocomplete(temporaryProvider.routesAutocomplete.ifEmpty { providerDefaultRoutes.autocomplete })
              .setPosts(temporaryProvider.routesPosts.ifEmpty { providerDefaultRoutes.posts })
              .setTags(temporaryProvider.routesTags.ifEmpty { providerDefaultRoutes.tags })
              .setComments(temporaryProvider.routesComments.ifEmpty { providerDefaultRoutes.comments })
              .setAuthSuffix(temporaryProvider.routesAuth.ifEmpty { providerDefaultRoutes.authSuffix ?: "" })
          )
          .build()
        onDone(newProvider)
      }
    ) {
      Text("Done")
    }
  }
}

data class TemporaryProvider(
  val name: String = "",
  val routesBase: String = "",
  val routesAutocomplete: String = "",
  val routesPosts: String = "",
  val routesTags: String = "",
  val routesComments: String = "",
  val routesAuth: String = "",
) {
  fun toProto(providerType: Provider.ProviderType): Provider {
    return Provider.newBuilder()
      .setName(name)
      .setProviderType(providerType)
      .setRoutes(Provider.Routes.newBuilder()
        .setBase(routesBase)
        .setAutocomplete(routesAutocomplete)
        .setPosts(routesPosts)
        .setTags(routesTags)
        .setComments(routesComments)
        .setAuthSuffix(routesAuth)
      )
      .build()
  }
}
fun Provider.toTemporary(): TemporaryProvider {
  return TemporaryProvider(
    name = this.name,
    routesBase = this.routes.base,
    routesAutocomplete = this.routes.autocomplete,
    routesPosts = this.routes.posts,
    routesTags = this.routes.tags,
    routesComments = this.routes.comments,
    routesAuth = this.routes.authSuffix,
  )
}

@Composable
@FullPreview
fun ProviderInfoDialogContentPreview() {
  JetSnatcherTheme {
    var providerType by remember { mutableStateOf(Providers.Gelbooru) }
    ProviderInfoDialogContent(
      onDone = {},
      providerType = providerType,
      onSelectProviderType = { /* Show [ProviderTypeDialog] */ },
    )
  }
}

@Composable
fun ProviderTypeDialog(
  onDismissRequest: () -> Unit,
  onSelectProvider: (provider: Providers) -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
  ) {
    ProviderTypeDialogContent(
      onSelectProvider = onSelectProvider,
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProviderTypeDialogContent(
  onSelectProvider: (provider: Providers) -> Unit,
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
      items(Providers.entries) { provider ->
        Text(
          text = provider.getFormattedName(),
          color = MaterialTheme.colorScheme.onPrimary,
          style = MaterialTheme.typography.labelLargeEmphasized,
          modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onSelectProvider(provider) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
@FullPreview
fun ProviderTypeDialogPreview() {
  JetSnatcherTheme {
    ProviderTypeDialogContent(
      onSelectProvider = {}
    )
  }
}