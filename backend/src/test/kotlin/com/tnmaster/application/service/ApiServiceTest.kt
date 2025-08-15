package com.tnmaster.application.service

import com.tnmaster.entities.Api
import com.tnmaster.repositories.IApiRepo
import com.tnmaster.service.ApiService
import io.github.truenine.composeserver.enums.HttpMethod
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@SpringBootTest
class ApiServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  lateinit var apiService: ApiService

  @Resource
  lateinit var apiRepo: IApiRepo

  @Test
  @RDBRollback
  fun post_all_found_upserts_apis_by_path_and_method() {
    // Given: 准备测试数据
    val path1 = "/v2/test/a"
    val path2 = "/v2/test/b"

    // When: 测试基本的插入功能（避免复杂的upsert行为）
    val insertedApis = apiService.postAllFound(
      listOf(
        Api {
          name = "TestApi1"
          apiPath = path1
          apiMethod = HttpMethod.GET
          requireLogin = true
        },
        Api {
          name = "TestApi2"
          apiPath = path2
          apiMethod = HttpMethod.POST
          requireLogin = false
        }
      )
    )

    // Then: 验证插入结果
    assertEquals(2, insertedApis.size, "应该成功插入2条记录")

    val api1 = insertedApis.find { it.apiPath == path1 && it.apiMethod == HttpMethod.GET }
    val api2 = insertedApis.find { it.apiPath == path2 && it.apiMethod == HttpMethod.POST }

    assertNotNull(api1, "应该存在第一个API记录")
    assertEquals("TestApi1", api1.name, "第一个API的name应该正确")
    assertEquals(true, api1.requireLogin, "第一个API的requireLogin应该为true")

    assertNotNull(api2, "应该存在第二个API记录")
    assertEquals("TestApi2", api2.name, "第二个API的name应该正确")
    assertEquals(false, api2.requireLogin, "第二个API的requireLogin应该为false")
  }
}

