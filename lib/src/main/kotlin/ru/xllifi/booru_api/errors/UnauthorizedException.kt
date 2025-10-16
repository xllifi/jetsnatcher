package ru.xllifi.booru_api.errors

import ru.xllifi.booru_api.Providers

class UnauthorizedException(
    val providerType: Providers,
    val url: String,
    val respBody: String,
) : Exception() {
    override fun toString(): String {
        return "${this.javaClass.name} from URL ${this.url}"
    }

}