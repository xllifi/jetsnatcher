package ru.xllifi.jetsnatcher.proto.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryProto(
  val entries: List<HistoryEntryProto> = emptyList(),
)