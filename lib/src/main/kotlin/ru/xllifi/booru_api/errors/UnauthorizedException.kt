package ru.xllifi.booru_api.errors

import ru.xllifi.booru_api.ProviderType

class UnauthorizedException(
    val providerType: ProviderType,
    val url: String,
    val respBody: String,
) : Exception() {
    override fun toString(): String {
        return "${this.javaClass.name} from URL ${this.url}"
    }

}