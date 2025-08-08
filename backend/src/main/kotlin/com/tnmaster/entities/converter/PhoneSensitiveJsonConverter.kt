package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class PhoneSensitiveJsonConverter : Converter<String, String> {
  override fun input(jsonValue: String) = jsonValue

  override fun output(value: String): String {
    return value.let { if (it.length == 11) it.substring(0, 3) + "****" + it.substring(it.length - 4) else "*****" }
  }
}
