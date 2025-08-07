package com.tnmaster.db.migration

import jakarta.annotation.Resource
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import kotlin.test.assertEquals

@SpringBootTest
class V2038MigrationTest : IDatabasePostgresqlContainer {

  @Resource
  lateinit var jdbcTemplate: JdbcTemplate

  @Test
  fun `所有表的 rlv 字段类型应为 int，ldf 字段类型应为 timestamp`() {
    val tableNames = jdbcTemplate.query(
      "select tablename from pg_tables where schemaname = 'public'", ResultSetExtractor { rs ->
        val list = mutableListOf<String>()
        while (rs.next()) {
          list.add(rs.getString(1))
        }
        list
      })
    assertNotNull(tableNames)
    for (tableName in tableNames) {
      val columns = jdbcTemplate.query(
        "select column_name, data_type from information_schema.columns where table_schema = 'public' and table_name = ?", ResultSetExtractor { rs ->
          val map = mutableMapOf<String, String>()
          while (rs.next()) {
            map[rs.getString("column_name").lowercase()] = rs.getString("data_type")
          }
          map
        }, tableName
      )
      assertNotNull(columns)
      columns["rlv"]?.let { rlvType ->
        assertEquals("integer", rlvType, "表 $tableName 的 rlv 字段类型不是 int，而是 $rlvType")
      }
      columns["ldf"]?.let { ldfType ->
        assert(ldfType.startsWith("timestamp")) { "表 $tableName 的 ldf 字段类型不是 timestamp，而是 $ldfType" }
      }
    }
  }
}
