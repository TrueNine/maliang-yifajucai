package com.tnmaster.entities.converter

import org.babyfish.jimmer.jackson.Converter
import java.time.Period

class PeriodToStringConverter : Converter<Period, String?> {
  override fun output(value: Period): String {
    return value.toString()
  }
}
