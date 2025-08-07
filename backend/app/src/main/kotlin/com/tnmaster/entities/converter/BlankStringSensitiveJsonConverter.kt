package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class BlankStringSensitiveJsonConverter : Converter<String, String> {
  override fun input(jsonValue: String): String {
    return jsonValue
  }

  override fun output(value: String): String {
    return ""
  }
}
