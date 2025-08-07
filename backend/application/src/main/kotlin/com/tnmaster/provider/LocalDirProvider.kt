package com.tnmaster.provider

import java.nio.file.Path
import java.nio.file.Paths

interface LocalFileProvider {
  val userDir: String
  val tempDir: String
  val logDir: String

  val logDirPath: Path?
    get() = Paths.get(userDir, logDir)
}

class SyncLocalFileProvider(override val logDir: String) : LocalFileProvider {
  override val userDir: String = System.getProperty("user.dir")
  override val tempDir: String = System.getProperty("java.io.tmpdir")
}
