package ru.xllifi.jetsnatcher.extensions

import ru.xllifi.booru_api.Providers
import ru.xllifi.booru_api.Routes
import ru.xllifi.booru_api.gelbooru.Gelbooru
import ru.xllifi.booru_api.rule34xxx.Rule34xxx
import ru.xllifi.jetsnatcher.proto.Provider

fun Provider.toReal(): ru.xllifi.booru_api.Provider {
  return when (this.providerType.toReal()) {
    Providers.Gelbooru -> Gelbooru(routes = this.routes.toReal())
    Providers.Rule34xxx -> Rule34xxx(routes = this.routes.toReal())
  }
}

fun Providers.toProto(): Provider.ProviderType {
  return when (this) {
    Providers.Gelbooru -> Provider.ProviderType.GELBOORU
    Providers.Rule34xxx -> Provider.ProviderType.R34XXX
  }
}

fun Provider.ProviderType.toReal(): Providers {
  return when (this) {
    Provider.ProviderType.GELBOORU -> Providers.Gelbooru
    Provider.ProviderType.R34XXX -> Providers.Rule34xxx
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