package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class IdCardSensitiveJsonConverter : Converter<String, String> {
  override fun input(jsonValue: String) = jsonValue

  override fun output(value: String): String {
    return value.let { if (it.length == 18) it.substring(0, 2) + "****" + it.substring(it.length - 2) else "*****" }
  }
}
