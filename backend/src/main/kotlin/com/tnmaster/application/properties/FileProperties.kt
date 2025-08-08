package com.tnmaster.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tnmaster.file")
class FileProperties {
  var logDir: String = ".logs"
}
