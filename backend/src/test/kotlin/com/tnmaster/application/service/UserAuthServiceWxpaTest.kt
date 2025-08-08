package com.tnmaster.application.service

import com.tnmaster.dto.userinfo.UserInfoAdminAssignAccountView
import com.tnmaster.entities.UserInfo
import com.tnmaster.application.repositories.IDisInfoRepo
import com.tnmaster.application.repositories.IUserAccountRepo
import com.tnmaster.application.repositories.IUserInfoRepo
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.psdk.wxpa.model.WxpaUserInfo
import io.github.truenine.composeserver.psdk.wxpa.service.WxpaService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserAuthServiceWxpaTest {

  private lateinit var userAccountRepo: IUserAccountRepo
  private lateinit var userInfoRepo: IUserInfoRepo
  private lateinit var passwordEncoder: PasswordEncoder
  private lateinit var disInfoRepo: IDisInfoRepo
  private lateinit var saService: SaTokenService
  private lateinit var wxpaService: WxpaService
  private lateinit var userAuthService: UserAuthService
  private lateinit var mockRequest: HttpServletRequest

  @BeforeEach
  fun setup() {
    userAccountRepo = mockk()
    userInfoRepo = mockk()
    passwordEncoder = mockk()
    disInfoRepo = mockk()
    saService = mockk()
    wxpaService = mockk()
    mockRequest = mockk()
    
    userAuthService = UserAuthService(
      userAccountRepo,
      userInfoRepo,
      passwordEncoder,
      disInfoRepo,
      saService,
      wxpaService
    )

    // Mock request properties
    every { mockRequest.remoteAddr } returns "127.0.0.1"
  }

  @Nested
  inner class `微信公众号登录测试` {

    @Test
    fun `应该成功通过微信code登录已注册用户`() {
      // Given
      val jsApiCode = "test_js_api_code"
      val openId = "test_open_id"
      val nickname = "测试用户"
      
      val wxpaUserInfo = WxpaUserInfo(
        openId = openId,
        nickname = nickname,
        privilege = emptyList(),
        unionId = "test_union_id"
      )
      
      val existingUserAccount = mockk<com.tnmaster.entities.UserAccount>()
      every { existingUserAccount.account } returns "test_account"

      val expectedLoginView = SaTokenService.SaTokenLoginView(
        getHeaderName = "Authorization",
        tokenTimeout = null,
        activeTimeout = null,
        account = "test_account",
        roles = emptySet(),
        permissions = emptySet()
      )

      every { wxpaService.getUserInfoByAuthCode(jsApiCode) } returns wxpaUserInfo
      every { userAuthService.fetchRegisteredAccountByWxpaOpenId(openId) } returns existingUserAccount
      every { userAuthService.loginByAccountOrThrow("test_account", mockRequest, null) } returns expectedLoginView

      // When
      val result = userAuthService.loginOrRegisteredByWxpaJsApiCodeOrThrow(jsApiCode, null, mockRequest)

      // Then
      assertNotNull(result)
      assertEquals(expectedLoginView.account, result.account)
      verify { wxpaService.getUserInfoByAuthCode(jsApiCode) }
      verify { userAuthService.fetchRegisteredAccountByWxpaOpenId(openId) }
      verify { userAuthService.loginByAccountOrThrow("test_account", mockRequest, null) }
    }

    @Test
    fun `应该成功通过微信code注册并登录新用户`() {
      // Given
      val jsApiCode = "test_js_api_code"
      val openId = "new_open_id"
      val nickname = "新用户"
      
      val wxpaUserInfo = WxpaUserInfo(
        openId = openId,
        nickname = nickname,
        privilege = emptyList(),
        unionId = "test_union_id"
      )
      
      val newUserInfo = mockk<UserInfo>()
      val mockAccount = mockk<com.tnmaster.entities.UserAccount>()
      every { newUserInfo.account } returns mockAccount
      every { mockAccount.account } returns "new_account"
      
      val expectedLoginView = SaTokenService.SaTokenLoginView(
        getHeaderName = "Authorization",
        tokenTimeout = null,
        activeTimeout = null,
        account = "new_account",
        roles = emptySet(),
        permissions = emptySet()
      )

      every { wxpaService.getUserInfoByAuthCode(jsApiCode) } returns wxpaUserInfo
      every { userAuthService.fetchRegisteredAccountByWxpaOpenId(openId) } returns null
      every { userAuthService.registerAccountByWxpaOpenId(openId, nickname) } returns newUserInfo
      every { userAuthService.loginByAccountOrThrow("new_account", mockRequest, null) } returns expectedLoginView

      // When
      val result = userAuthService.loginOrRegisteredByWxpaJsApiCodeOrThrow(jsApiCode, null, mockRequest)

      // Then
      assertNotNull(result)
      assertEquals(expectedLoginView.account, result.account)
      verify { wxpaService.getUserInfoByAuthCode(jsApiCode) }
      verify { userAuthService.fetchRegisteredAccountByWxpaOpenId(openId) }
      verify { userAuthService.registerAccountByWxpaOpenId(openId, nickname) }
      verify { userAuthService.loginByAccountOrThrow("new_account", mockRequest, null) }
    }

    @Test
    fun `当微信用户信息获取失败时应该返回null`() {
      // Given
      val jsApiCode = "invalid_code"
      every { wxpaService.getUserInfoByAuthCode(jsApiCode) } returns null

      // When
      val result = userAuthService.loginOrRegisteredByWxpaJsApiCodeOrThrow(jsApiCode, null, mockRequest)

      // Then
      assertNull(result)
      verify { wxpaService.getUserInfoByAuthCode(jsApiCode) }
    }

    @Test
    fun `当微信用户昵称为空时应该使用默认昵称`() {
      // Given
      val jsApiCode = "test_js_api_code"
      val openId = "test_open_id"
      
      val wxpaUserInfo = WxpaUserInfo(
        openId = openId,
        nickname = null, // 昵称为空
        privilege = emptyList(),
        unionId = "test_union_id"
      )
      
      val newUserInfo = mockk<UserInfo>()
      val mockAccount = mockk<com.tnmaster.entities.UserAccount>()
      every { newUserInfo.account } returns mockAccount
      every { mockAccount.account } returns "new_account"
      
      val expectedLoginView = SaTokenService.SaTokenLoginView(
        getHeaderName = "Authorization",
        tokenTimeout = null,
        activeTimeout = null,
        account = "new_account",
        roles = emptySet(),
        permissions = emptySet()
      )

      every { wxpaService.getUserInfoByAuthCode(jsApiCode) } returns wxpaUserInfo
      every { userAuthService.fetchRegisteredAccountByWxpaOpenId(openId) } returns null
      every { userAuthService.registerAccountByWxpaOpenId(openId, "微信用户") } returns newUserInfo
      every { userAuthService.loginByAccountOrThrow("new_account", mockRequest, null) } returns expectedLoginView

      // When
      val result = userAuthService.loginOrRegisteredByWxpaJsApiCodeOrThrow(jsApiCode, null, mockRequest)

      // Then
      assertNotNull(result)
      verify { userAuthService.registerAccountByWxpaOpenId(openId, "微信用户") }
    }
  }
}
