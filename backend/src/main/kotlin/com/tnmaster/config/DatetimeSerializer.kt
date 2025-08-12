package com.tnmaster.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import io.github.truenine.composeserver.datetime
import java.io.IOException

/**
 * datetime类型的序列化器
 */
class DatetimeSerializer : JsonSerializer<datetime>() {
  @Throws(IOException::class)
  override fun serialize(value: datetime?, gen: JsonGenerator, serializers: SerializerProvider) {
    if (value == null) {
      gen.writeNull()
    } else {
      gen.writeString(value.toString())
    }
  }

  @Throws(IOException::class)
  override fun serializeWithType(
    value: datetime?,
    gen: JsonGenerator,
    serializers: SerializerProvider,
    typeSer: TypeSerializer
  ) {
    // 对于带类型信息的序列化，直接调用标准序列化方法
    // TypeSerializer会自动处理类型信息的包装
    serialize(value, gen, serializers)
  }
}

/**
 * datetime类型的反序列化器
 */
class DatetimeDeserializer : JsonDeserializer<datetime>() {
  @Throws(IOException::class)
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): datetime? {
    val text = p.text
    return if (text.isNullOrBlank()) {
      null
    } else {
      try {
        datetime.parse(text)
      } catch (e: Exception) {
        throw IOException("无法解析datetime: $text", e)
      }
    }
  }
}
