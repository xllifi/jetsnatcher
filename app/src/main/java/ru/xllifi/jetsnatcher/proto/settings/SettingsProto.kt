package ru.xllifi.jetsnatcher.proto.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SettingsProto(
  @ProtoNumber(1) val pageSize: UInt = 20u,
  @ProtoNumber(2) val providers: List<ProviderProto> = emptyList(),
  @ProtoNumber(3) val doubleTapThreshold: UInt = 300u,
  @ProtoNumber(4) val showCardInfo: Boolean = true,
  @ProtoNumber(5) val blacklistedTags: List<BlacklistedTagProto> = emptyList(),
)