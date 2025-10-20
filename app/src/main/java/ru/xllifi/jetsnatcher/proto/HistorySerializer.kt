package ru.xllifi.jetsnatcher.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import ru.xllifi.jetsnatcher.proto.history.HistoryProto
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
object HistorySerializer : Serializer<HistoryProto> {
  override val defaultValue: HistoryProto = HistoryProto()

  override suspend fun readFrom(input: InputStream): HistoryProto {
    try {
      return ProtoBuf.decodeFromByteArray(input.readBytes())
    } catch (ex: Exception) {
      when (ex) {
        is SerializationException, is IllegalArgumentException -> {
          throw CorruptionException("Cannot read proto.", ex)
        }
        else -> throw ex
      }
    }
  }

  override suspend fun writeTo(t: HistoryProto, output: OutputStream) =
    output.write(ProtoBuf.encodeToByteArray(t))
}

val Context.historyDataStore: DataStore<HistoryProto> by dataStore(
  fileName = "history.pb",
  serializer = HistorySerializer
)