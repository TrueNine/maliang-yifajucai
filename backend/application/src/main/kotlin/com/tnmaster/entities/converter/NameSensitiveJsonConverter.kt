package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class NameSensitiveJsonConverter : Converter<String, String> {
  override fun input(jsonValue: String): String {
    return jsonValue
  }

  override fun output(value: String): String {
    if (value.isBlank()) return ""
    return when (value.length) {
      1 -> "*"
      2 -> "*${value[1]}"
      else -> "${value[0]}*${value[value.length - 1]}"
    }
  }
}
