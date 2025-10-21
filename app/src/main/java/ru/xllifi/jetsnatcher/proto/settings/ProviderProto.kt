package ru.xllifi.jetsnatcher.proto.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.Routes
import ru.xllifi.booru_api.gelbooru.Gelbooru

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProviderProto(
  @ProtoNumber(1) val name: String = "",
  @ProtoNumber(2) val providerType: ProviderType = ProviderType.Gelbooru,
  @ProtoNumber(3) val routes: Routes = Gelbooru.defaultRoutes,
)