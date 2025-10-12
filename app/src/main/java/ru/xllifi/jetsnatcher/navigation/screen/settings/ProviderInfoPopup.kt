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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.xllifi.booru_api.Providers
import ru.xllifi.jetsnatcher.extensions.FullPreview
import ru.xllifi.jetsnatcher.proto.Provider
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val providerMapping: Map<Providers, Provider.ProviderType> = mapOf(
  Providers.Gelbooru to Provider.ProviderType.GELBOORU,
  Providers.Rule34xxx to Provider.ProviderType.R34XXX,
)

@Composable
private fun TextField(
  value: String,
  onValueChange: (newVal: String) -> Unit,
  placeholder: String,
  onDone: (value: String) -> Unit,
  icon: @Composable () -> Unit = {},
) {
  BasicTextField(
    value = value,
    onValueChange = { onValueChange(it) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions { onDone(value) },
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.small)
      .background(MaterialTheme.colorScheme.surfaceContainer),
    textStyle = MaterialTheme.typography.bodyLarge.copy(
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 16.sp,
      lineHeight = 16.sp,
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .height(40.dp)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        icon()
        Box {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
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
}

@Composable
fun ProviderInfoDialog(
  onDismissRequest: () -> Unit,
  providerType: Providers,
  /** This should show [ProviderTypeDialog] */
  onSelectProviderType: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
  ) {
    ProviderInfoDialogContent(
      providerType = providerType,
      onSelectProviderType = onSelectProviderType,
    )
  }
}

@Composable
fun ProviderInfoDialogContent(
  provider: Provider? = null,
  providerType: Providers,
  onSelectProviderType: () -> Unit,
) {
  var name by remember { mutableStateOf(provider?.name ?: "") }
  var routesBase by remember { mutableStateOf(provider?.routes?.base ?: "") }
  var routesAutocomplete by remember { mutableStateOf(provider?.routes?.autocomplete ?: "") }
  var routesPosts by remember { mutableStateOf(provider?.routes?.posts ?: "") }
  var routesTags by remember { mutableStateOf(provider?.routes?.tags ?: "") }
  var routesComments by remember { mutableStateOf(provider?.routes?.comments ?: "") }
  var routesAuth by remember { mutableStateOf(provider?.routes?.authSuffix ?: "") }
  Column(
    modifier = Modifier
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    TextField(
      value = name,
      onValueChange = { name = it },
      placeholder = "Name",
      onDone = { name = it },
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
  }
  val type: Provider.ProviderType? = providerMapping[providerType]
  if (type == null) {
    Log.e("JetSnatcher_PROVIDER_INFO_DIALOG", "No such provider ${providerType.getFormattedName()}!")
  }
  val pr = {
    Provider.newBuilder()
      .setName(name)
      .setProviderType(type)
      .setRoutes(
        Provider.Routes.newBuilder()
          .setBase(routesBase)
          .setAutocomplete(routesAutocomplete)
          .setPosts(routesPosts)
          .setTags(routesTags)
          .setComments(routesComments)
          .setAuthSuffix(routesAuth)
      )
      .build()
  }()
}

@Composable
@FullPreview
fun ProviderInfoDialogContentPreview() {
  JetSnatcherTheme {
    var providerType by remember { mutableStateOf(Providers.Gelbooru) }
    ProviderInfoDialogContent(
      providerType = providerType,
      onSelectProviderType = { /* Show ProviderTypeDialog */ }
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
      .padding(12.dp)
    ,
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