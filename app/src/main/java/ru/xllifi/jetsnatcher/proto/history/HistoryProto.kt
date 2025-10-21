package ru.xllifi.jetsnatcher.proto.history

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class HistoryProto(
  @ProtoNumber(1) val entries: List<HistoryEntryProto> = emptyList(),
)