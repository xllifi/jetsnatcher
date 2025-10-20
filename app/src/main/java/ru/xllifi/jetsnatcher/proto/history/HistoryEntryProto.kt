package ru.xllifi.jetsnatcher.proto.history

import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.Tag

@Serializable
data class HistoryEntryProto(
  val createdAt: Long,
  val tags: List<Tag>,
)
