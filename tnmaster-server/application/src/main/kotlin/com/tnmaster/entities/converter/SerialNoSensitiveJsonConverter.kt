package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class SerialNoSensitiveJsonConverter : Converter<String, String> {
  override fun output(value: String): String {
    if (value.isBlank()) return value
    return value.let { serialNo ->
      when (serialNo.length) {
        1 -> "**"
        2 -> "**"
        3 -> "**"
        4 -> "**"
        5 -> "**"
        else -> "${serialNo.substring(0, 2)}****${serialNo.substring(serialNo.length - 2)}"
      }
    }
  }

  override fun input(jsonValue: String): String {
    return jsonValue
  }
}
