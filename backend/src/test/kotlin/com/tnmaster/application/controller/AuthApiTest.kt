package com.tnmaster.application.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tnmaster.application.config.TestWebMvcConfiguration
import com.tnmaster.entities.RoleGroup
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
import com.tnmaster.application.repositories.IRoleGroupRepo
import com.tnmaster.application.repositories.IUserAccountRepo
import com.tnmaster.application.repositories.IUserInfoRepo
import jakarta.annotation.Resource
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.sql.DataSource


@RDBRollback
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=io.github.truenine.composeserver.oss.minio.autoconfig.MinioAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestWebMvcConfiguration::class)
class AuthApiTest : IDatabasePostgresqlContainer, ICacheRedisContainer {
  // 公共扩展函数，自动加 user-agent 和 ip
  fun MockHttpServletRequestBuilder.withCommonHeaders(
    userAgent: String = "tnmaster-test-agent",
    ip: String = "127.0.0.1"
  ): MockHttpServletRequestBuilder {
    return this
      .header("User-Agent", userAgent)
      .header("X-Forwarded-For", ip)
      .header("X-Real-IP", ip)
  }


  companion object {
    @JvmStatic
    private val log = io.github.truenine.composeserver.slf4j<AuthApiTest>()
  }

  @Resource
  lateinit var mockMvc: MockMvc

  @Resource
  lateinit var objectMapper: ObjectMapper

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @Resource
  lateinit var userInfoRepo: IUserInfoRepo

  @Resource
  lateinit var roleGroupRepo: IRoleGroupRepo

  @Resource
  lateinit var dataSource: DataSource

  @BeforeEach
  @ACID
  fun setupUser() {
    userAccountRepo.findUserAccountByAccount("user1")?.also {
      userAccountRepo.deleteById(it.id)
    }
    // 获取或创建基础角色组
    val userRoleGroup = roleGroupRepo.findAll().find { it.name == "USER" }
      ?: roleGroupRepo.saveCommand(RoleGroup {
        name = "USER"
        doc = "基础用户组"
      }).execute().modifiedEntity

    val bCryptPassword = BCryptPasswordEncoder().encode("password")
    log.info("[TEST-LOG] 生成的 user1 密码密文: {}", bCryptPassword)
    val userAccount = userAccountRepo.saveCommand(UserAccount {
      account = "user1"
      pwdEnc = bCryptPassword
      roleGroups = listOf(userRoleGroup)
    }).execute().modifiedEntity

    log.info("[TEST-LOG] 写入后 user1 账号对象: {}", userAccount)
    val userAccountDb = userAccountRepo.findUserAccountByAccount("user1")
    log.info("[TEST-LOG] findUserAccountByAccount(user1) = {}", userAccountDb)
    val pwdEnc = userAccountRepo.findPwdEncByAccount("user1")
    log.info("[TEST-LOG] findPwdEncByAccount(user1) = {}", pwdEnc)

    userInfoRepo.saveCommand(UserInfo {
      userAccountId = userAccount.id
      firstName = "测试"
      lastName = "用户"
      phone = "13800000000"
    }).execute()

    // 打印 job_seeker_status 表的所有字段名
    dataSource.connection.use { conn ->
      val meta = conn.metaData
      val rs = meta.getColumns(null, null, "job_seeker_status", null)
      val columns = mutableListOf<String>()
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"))
      }
      log.info("[TEST-LOG] job_seeker_status 表字段: {}", columns)
    }
  }

  @AfterEach
  fun cleanupUser() {
    userAccountRepo.findUserAccountByAccount("user1")?.let {
      userAccountRepo.deleteById(it.id)
    }
  }

  @Test
  @ACID
  fun `loginBySystemAccount 正常登录`() {
    val dto = AuthApi.AccountDto(account = "user1", password = "cGFzc3dvcmQ=")
    mockMvc.perform(
      MockMvcRequestBuilders.post("/v2/auth/login/account")
        .withCommonHeaders()
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.account").value("user1"))
      .andExpect(jsonPath("$.token").exists())
      .andExpect(jsonPath("$.login").value(true))
  }

  @Test
  fun `loginBySystemAccount 账号为空`() {
    val dto = AuthApi.AccountDto(account = null, password = "cGFzc3dvcmQ=")
    mockMvc.perform(
      MockMvcRequestBuilders.post("/v2/auth/login/account")
        .withCommonHeaders()
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.msg").value("Bad Request"))
  }

  @Test
  fun `loginBySystemAccount 密码为空`() {
    val dto = AuthApi.AccountDto(account = "user1", password = null)
    mockMvc.perform(
      MockMvcRequestBuilders.post("/v2/auth/login/account")
        .withCommonHeaders()
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.msg").value("Bad Request"))
  }

  @Test
  fun `loginBySystemAccount service 抛出异常`() {
    val dto = AuthApi.AccountDto(account = "user1", password = "cGFzc3dvcmQ=")
    mockMvc.perform(
      MockMvcRequestBuilders.post("/v2/auth/login/account")
        .withCommonHeaders()
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.msg").value("Bad Request"))
  }
} 
