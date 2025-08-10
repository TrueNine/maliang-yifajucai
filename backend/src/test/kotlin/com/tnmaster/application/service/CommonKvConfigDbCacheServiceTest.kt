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
    fun `首次获取应返回默认百家姓并持久化`() {
      val set = cacheService.fetchChinaFirstNames()
      assertTrue(set.isNotEmpty())
      // 再次获取应从缓存表读取
      val set2 = cacheService.fetchChinaFirstNames()
      assertTrue(set2.isNotEmpty())
    }

    @Test
    @RDBRollback
    fun `添加和删除百家姓`() {
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
  fun `get 与 postString 与 get-typed`() {
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

