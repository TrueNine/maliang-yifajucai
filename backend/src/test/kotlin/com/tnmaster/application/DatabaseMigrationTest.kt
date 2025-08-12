package com.tnmaster.application

import com.tnmaster.application.config.TestWebMvcConfiguration
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource
import jakarta.annotation.Resource
import kotlin.test.assertTrue

/**
 * 数据库迁移测试
 * 验证 Flyway 迁移是否正确执行
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebMvcConfiguration::class)
@Testcontainers
class DatabaseMigrationTest : IDatabasePostgresqlContainer, ICacheRedisContainer, IOssMinioContainer {

  @Resource
  lateinit var dataSource: DataSource

  @Test
  fun verify_basic_tables_exist() {
    dataSource.connection.use { connection ->
      val statement = connection.createStatement()
      
      // 检查 user_account 表是否存在
      val userAccountResult = statement.executeQuery(
        "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_account')"
      )
      userAccountResult.next()
      assertTrue(userAccountResult.getBoolean(1), "user_account 表应该存在")
      
      // 检查 role_group 表是否存在
      val roleGroupResult = statement.executeQuery(
        "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'role_group')"
      )
      roleGroupResult.next()
      assertTrue(roleGroupResult.getBoolean(1), "role_group 表应该存在")
      
      // 检查 role 表是否存在
      val roleResult = statement.executeQuery(
        "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'role')"
      )
      roleResult.next()
      assertTrue(roleResult.getBoolean(1), "role 表应该存在")
    }
  }
}
