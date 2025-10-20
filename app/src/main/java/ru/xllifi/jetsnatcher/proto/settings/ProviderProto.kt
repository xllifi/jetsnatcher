package ru.xllifi.jetsnatcher.proto.settings

import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.Routes
import ru.xllifi.booru_api.gelbooru.Gelbooru

@Serializable
data class ProviderProto(
  val name: String = "",
  val providerType: ProviderType = ProviderType.Gelbooru,
  val routes: Routes = Gelbooru.defaultRoutes,
)