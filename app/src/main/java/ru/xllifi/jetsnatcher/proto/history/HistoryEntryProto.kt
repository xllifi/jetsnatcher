package ru.xllifi.jetsnatcher.proto.history

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import ru.xllifi.booru_api.Tag

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class HistoryEntryProto(
  @ProtoNumber(1) val createdAt: Long,
  @ProtoNumber(2) val tags: List<Tag>,
)
