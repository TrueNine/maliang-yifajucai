package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter

class DisCardSensitiveJsonConverter : Converter<String, String> {
  override fun input(jsonValue: String) = jsonValue

  override fun output(value: String): String {
    return value.let { if (it.length == 20 || it.length == 22) it.substring(0, 2) + "****" + it.substring(it.length - 2) else "*****" }
  }
}
