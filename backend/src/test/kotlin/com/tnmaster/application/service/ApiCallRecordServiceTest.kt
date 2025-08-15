package com.tnmaster.application.service

import com.tnmaster.entities.ApiCallRecord
import com.tnmaster.repositories.IApiCallRecordRepo
import com.tnmaster.service.ApiCallRecordService
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.context.annotation.Import
import com.tnmaster.application.config.BaseRedisTest
import com.tnmaster.application.config.TestOssConfiguration
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertTrue

@TestPropertySource(
  properties = [
    "spring.autoconfigure.exclude=io.github.truenine.composeserver.oss.minio.autoconfig.MinioAutoConfiguration",
    "spring.main.allow-bean-definition-overriding=true"
  ]
)
@Import(TestOssConfiguration::class)
class ApiCallRecordServiceTest : BaseRedisTest(), IDatabasePostgresqlContainer {

  @Resource
  lateinit var service: ApiCallRecordService

  @Resource
  lateinit var repo: IApiCallRecordRepo

  @Test
  @Transactional
  @Commit
  fun post_to_cache_and_scheduled_save_writes_records_to_db() = runBlocking {
    // Given: add two records into cache
    val record1 = ApiCallRecord {
      reqProtocol = "HTTP/1.1"
      reqMethod = "GET"
      reqPath = "/health"
      reqDatetime = datetime.now()
      respDatetime = datetime.now()
      respCode = 200
      deviceCode = "dev1"
      loginIp = "127.0.0.1"
      reqIp = "127.0.0.1"
    }

    println("DEBUG: 准备缓存记录1: $record1")
    service.postToCache(record1)
    service.postToCache(
      ApiCallRecord {
        reqProtocol = "HTTP/1.1"
        reqMethod = "POST"
        reqPath = "/login"
        reqDatetime = datetime.now()
        respDatetime = datetime.now()
        respCode = 401
        deviceCode = "dev2"
        loginIp = "127.0.0.1"
        reqIp = "127.0.0.1"
      }
    )

    // When: trigger scheduled persist and cleanup
    service.scheduledCacheSaveToDatabase()

    // Then: DB has at least one of them
    val list = repo.findAll()
    assertTrue(list.isNotEmpty())
    service.clearCachedRecords()
  }
}
