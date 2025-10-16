package ru.xllifi.jetsnatcher.extensions

import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.Routes
import ru.xllifi.booru_api.gelbooru.Gelbooru
import ru.xllifi.booru_api.rule34xxx.Rule34xxx
import ru.xllifi.jetsnatcher.proto.Provider

fun Provider.toReal(): ru.xllifi.booru_api.Provider {
  return when (this.providerType.toReal()) {
    ProviderType.Gelbooru -> Gelbooru(routes = this.routes.toReal())
    ProviderType.Rule34xxx -> Rule34xxx(routes = this.routes.toReal())
  }
}

fun ProviderType.toProto(): Provider.ProviderType {
  return when (this) {
    ProviderType.Gelbooru -> Provider.ProviderType.GELBOORU
    ProviderType.Rule34xxx -> Provider.ProviderType.R34XXX
  }
}

fun Provider.ProviderType.toReal(): ProviderType {
  return when (this) {
    Provider.ProviderType.GELBOORU -> ProviderType.Gelbooru
    Provider.ProviderType.R34XXX -> ProviderType.Rule34xxx
    Provider.ProviderType.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized provider type")
  }
}

fun Provider.Routes.toReal(): Routes {
  return Routes(
    base = this.base,
    publicFacingPostPage = this.publicFacingPostPage,
    autocomplete = this.autocomplete,
    posts = this.posts,
    tags = this.tags,
    comments = this.comments,
    notes = this.notes,
    authSuffix = this.authSuffix,
  )
}