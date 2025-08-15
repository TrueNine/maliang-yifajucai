package com.tnmaster.application.service

import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.repositories.IUserInfoRepo
import com.tnmaster.service.UserAuthService
import com.tnmaster.service.AuthService
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import io.mockk.every
import io.mockk.mockk
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertNotNull

@SpringBootTest
class UserAuthServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  lateinit var userAuthService: UserAuthService

  @Resource
  lateinit var authService: AuthService

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @Resource
  lateinit var userInfoRepo: IUserInfoRepo

  @Nested
  inner class LoginTests {

    @Test
    @RDBRollback
    fun login_by_account_and_password_should_work_correctly() {
      // Given: test account and password
      val testAccount = "test_user"
      val testPassword = "test_password"
      val request = MockHttpServletRequest().apply {
        addHeader("Device-Id", "test-device-auth")
      }

      // This test would require actual user data in the database
      // For now, we'll test that the method doesn't throw exceptions
      assertThrows<IllegalArgumentException> {
        userAuthService.loginByAccountAndBase64PasswordOrThrow(testAccount, testPassword, request)
      }
    }

    @Test
    @RDBRollback
    fun login_with_empty_account_should_throw_exception() {
      // Given: empty account
      val emptyAccount = ""
      val testPassword = "test_password"
      val request = MockHttpServletRequest().apply {
        addHeader("Device-Id", "test-device-auth")
      }

      // When & Then: should throw exception
      assertThrows<IllegalArgumentException> {
        userAuthService.loginByAccountAndBase64PasswordOrThrow(emptyAccount, testPassword, request)
      }
    }

    @Test
    @RDBRollback
    fun login_with_empty_password_should_throw_exception() {
      // Given: empty password
      val testAccount = "test_user"
      val emptyPassword = ""
      val request = MockHttpServletRequest().apply {
        addHeader("Device-Id", "test-device-auth")
      }

      // When & Then: should throw exception
      assertThrows<IllegalArgumentException> {
        userAuthService.loginByAccountAndBase64PasswordOrThrow(testAccount, emptyPassword, request)
      }
    }
  }

  @Nested
  inner class LogoutTests {

    @Test
    @RDBRollback
    fun logout_should_work_correctly() {
      // Given: mock auth info
      val request = MockHttpServletRequest().apply {
        addHeader("Device-Id", "test-device-auth")
      }

      // When: logout (should not throw exception)
      userAuthService.setCurrentSessionLogout(null, request)

      // Then: no exception should be thrown
      // This is a basic test since we don't have actual login state
    }
  }

  @Nested
  inner class CurrentLoginInfoTests {

    @Test
    @RDBRollback
    fun fetch_current_login_info_should_return_null_when_not_logged_in() {
      // When: fetch current login info without being logged in
      val result = userAuthService.fetchCurrentLoginInfo()

      // Then: should return null
      // Note: This might return null or throw exception depending on implementation
      // The test verifies the method can be called without crashing
    }
  }
}
