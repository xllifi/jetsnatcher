package ru.xllifi.jetsnatcher.proto.settings

import kotlinx.serialization.Serializable

@Serializable
data class SettingsProto(
  val pageSize: UInt = 20u,
  val providers: List<ProviderProto> = emptyList(),
  val doubleTapThreshold: UInt = 300u,
  val showCardInfo: Boolean = true,
  val blacklistedTagValues: List<String> = emptyList(),
)