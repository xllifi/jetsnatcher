package ru.xllifi.jetsnatcher.extensions

import ru.xllifi.booru_api.Provider
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.gelbooru.Gelbooru
import ru.xllifi.booru_api.rule34xxx.Rule34xxx
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto

fun ProviderProto.toReal(): Provider {
  return when (this.providerType) {
    ProviderType.Gelbooru -> Gelbooru(routes = this.routes)
    ProviderType.Rule34xxx -> Rule34xxx(routes = this.routes)
  }
}