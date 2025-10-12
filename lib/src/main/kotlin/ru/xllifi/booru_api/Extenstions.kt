package ru.xllifi.booru_api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.toUnixTimestamp(parsingFormat: String): Long {
  return OffsetDateTime
    .parse(
      this,
      DateTimeFormatter.ofPattern(parsingFormat, Locale.US)
    )
    .toInstant().epochSecond
}

// TODO: tests
object IntOrNullSerializer : KSerializer<Int?> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntOrNull", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: Int?) {
    when (value) {
      null -> encoder.encodeString("")
      else -> encoder.encodeInt(value)
    }
  }

  override fun deserialize(decoder: Decoder): Int? {
    return when (val str = decoder.decodeString()) {
      "" -> null
      else -> str.toIntOrNull() ?: throw SerializationException("Invalid Int: \"$str\"")
    }
  }

}