package com.tnmaster.service

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil
import cn.dev33.satoken.servlet.util.SaTokenContextJakartaServletUtil
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.repositories.IUserInfoRepo
import io.mockk.every
import io.mockk.mockk
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class UserAuthServiceTest : IDatabasePostgresqlContainer, ICacheRedisContainer {

  @Resource
  lateinit var userAuthService: UserAuthService

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @Resource
  lateinit var userInfoRepo: IUserInfoRepo

  private fun createTestAccount(account: String, password: String): String {
    userAuthService.assignSystemAccount(account = account, password = password, nickName = account)
    return account
  }

  @Nested
  inner class LoginByAccountAndBase64PasswordOrThrowFunctionGroup {

    @Test
    @RDBRollback
    fun `正常 账号密码登录时，返回登录信息`() {
      SaTokenContextMockUtil.setMockContext {
        val req = MockHttpServletRequest()
        req.addHeader("User-Agent", "mock")

        val resp = MockHttpServletResponse()
        SaTokenContextJakartaServletUtil.setContext(
          req, resp
        )

        val account = "testuser1"
        val password = "testpass1"
        createTestAccount(account, password)
        val base64Password = java.util.Base64.getEncoder().encodeToString(password.toByteArray())

        val result = userAuthService.loginByAccountAndBase64PasswordOrThrow(account, base64Password, req)
        assertNotNull(result)
        assertEquals(account, result.account)
      }
    }

    @Test
    @RDBRollback
    fun `异常 账号为空时，抛出异常`() {
      SaTokenContextMockUtil.setMockContext {
        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        SaTokenContextJakartaServletUtil.setContext(
          req, resp
        )

        val password = "testpass2"
        val base64Password = java.util.Base64.getEncoder().encodeToString(password.toByteArray())
        val request: HttpServletRequest = MockHttpServletRequest()
        val ex = assertThrows<IllegalArgumentException> {
          userAuthService.loginByAccountAndBase64PasswordOrThrow("", base64Password, request)
        }
        assertEquals("account is blank", ex.message)
      }
    }

    @Test
    @RDBRollback
    fun `异常 密码为空时，抛出异常`() {
      SaTokenContextMockUtil.setMockContext {
        val account = "testuser3"
        createTestAccount(account, "testpass3")
        val request: HttpServletRequest = MockHttpServletRequest()
        val ex = assertThrows<IllegalArgumentException> {
          userAuthService.loginByAccountAndBase64PasswordOrThrow(account, "", request)
        }
        assertEquals("base64Password is blank", ex.message)
      }
    }

    @Test
    @RDBRollback
    fun `异常 密码错误时，抛出异常`() {
      SaTokenContextMockUtil.setMockContext {
        val account = "testuser4"
        val password = "testpass4"
        createTestAccount(account, password)
        val wrongPassword = "wrongpass"
        val base64Password = java.util.Base64.getEncoder().encodeToString(wrongPassword.toByteArray())
        val request: HttpServletRequest = MockHttpServletRequest()
        val ex = assertThrows<IllegalArgumentException> {
          userAuthService.loginByAccountAndBase64PasswordOrThrow(account, base64Password, request)
        }
        assertEquals("账号或密码错误", ex.message)
      }
    }
  }

  @Test
  fun `模拟 request 的 deviceId 和 remoteRequestIp`() {
    SaTokenContextMockUtil.setMockContext {
      val mockRequest = mockk<HttpServletRequest>(relaxed = true)
      val mockResponse = mockk<HttpServletResponse>(relaxed = true)
      every { mockRequest.getHeader("deviceId") } returns "test-device-id"
      every { mockRequest.remoteAddr } returns "127.0.0.1"
      every { mockRequest.getAttribute("remoteRequestIp") } returns "192.168.1.1"

      // 注入到 Sa-Token 上下文
      SaTokenContextJakartaServletUtil.setContext(mockRequest, mockResponse)

      // 断言模拟生效
      assert("test-device-id" == mockRequest.getHeader("deviceId"))
      assert("127.0.0.1" == mockRequest.remoteAddr)
      assert("192.168.1.1" == mockRequest.getAttribute("remoteRequestIp"))
    }
  }
} 
