package ru.xllifi.jetsnatcher.proto.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BlacklistedTagProto(
  @ProtoNumber(1) val createdAt: Long = 0,
  @ProtoNumber(2) val value: String = "",
)