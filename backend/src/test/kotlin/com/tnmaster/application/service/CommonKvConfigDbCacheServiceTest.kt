package com.tnmaster.application.service

import com.tnmaster.repositories.ICommonKvConfigDbCacheRepo
import com.tnmaster.service.CommonKvConfigDbCacheService
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class CommonKvConfigDbCacheServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  lateinit var cacheService: CommonKvConfigDbCacheService

  @Resource
  lateinit var cacheRepo: ICommonKvConfigDbCacheRepo

  @Nested
  inner class ChinaFirstNamesGroup {
    @Test
    @RDBRollback
    fun first_fetch_should_return_default_chinese_surnames_and_persist() {
      val set = cacheService.fetchChinaFirstNames()
      assertTrue(set.isNotEmpty())
      // 再次获取应从缓存表读取
      val set2 = cacheService.fetchChinaFirstNames()
      assertTrue(set2.isNotEmpty())
    }

    @Test
    @RDBRollback
    fun add_and_remove_chinese_surnames() {
      val base = cacheService.fetchChinaFirstNames()
      val added = cacheService.postChinaFirstName("增田")
      assertTrue(added.contains("增田"))
      val afterAdd = cacheService.fetchChinaFirstNames()
      assertTrue(afterAdd.contains("增田"))

      cacheService.removeChinaFirstName("增田")
      val afterRemove = cacheService.fetchChinaFirstNames()
      assertFalse(afterRemove.contains("增田"))
      // 基础集合仍存在
      assertTrue(afterRemove.isNotEmpty())
    }
  }

  @Test
  @RDBRollback
  fun get_and_post_string_and_get_typed() {
    val key = "custom.key"
    // 保存字符串
    cacheService.postString(key, "{\"a\":1}")
    val raw = cacheService[key]
    assertTrue(raw?.contains("\"a\":1") == true)

    // 保存对象并按类型读取
    val value = mapOf("x" to 2)
    cacheService.post(key, value)
    val typed: Map<*, *>? = cacheService[key, Map::class]
    assertNotNull(typed)
    assertTrue(typed?.get("x") == 2)
  }
}

