package com.tnmaster.application.apis

import com.tnmaster.apis.WechatPublicAccountApi
import io.github.truenine.composeserver.psdk.wxpa.model.WxpaSignature
import io.github.truenine.composeserver.psdk.wxpa.model.WxpaUserInfo
import io.github.truenine.composeserver.psdk.wxpa.service.WxpaService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WechatPublicAccountApiTest {

  private lateinit var wxpaService: WxpaService
  private lateinit var wechatPublicAccountApi: WechatPublicAccountApi

  @BeforeEach
  fun setup() {
    wxpaService = mockk()
    wechatPublicAccountApi = WechatPublicAccountApi(wxpaService)
  }

  @Nested
  inner class UserInfoFetchTests {

    @Test
    fun should_successfully_get_user_info_by_code() {
      // Given
      val testCode = "test_auth_code_123"
      val expectedUserInfo = WxpaUserInfo(
        openId = "test_open_id",
        nickname = "测试用户",
        privilege = emptyList(),
        unionId = "test_union_id"
      )
      every { wxpaService.getUserInfoByAuthCode(testCode) } returns expectedUserInfo

      // When
      val result = wechatPublicAccountApi.getUserInfoByCode(testCode)

      // Then
      assertNotNull(result)
      assertEquals(expectedUserInfo.openId, result.openId)
      assertEquals(expectedUserInfo.nickname, result.nickname)
      assertEquals(expectedUserInfo.unionId, result.unionId)
      verify { wxpaService.getUserInfoByAuthCode(testCode) }
    }

    @Test
    fun should_return_null_when_user_info_fetch_fails() {
      // Given
      val testCode = "invalid_code"
      every { wxpaService.getUserInfoByAuthCode(testCode) } returns null

      // When
      val result = wechatPublicAccountApi.getUserInfoByCode(testCode)

      // Then
      assertNull(result)
      verify { wxpaService.getUserInfoByAuthCode(testCode) }
    }
  }

  @Nested
  inner class ServerVerificationTests {

    @Test
    fun should_successfully_verify_server_configuration() {
      // Given
      val signature = "test_signature"
      val timestamp = "1234567890"
      val nonce = "test_nonce"
      val echostr = "test_echo_string"
      val expectedRequest = WxpaService.ServerVerificationRequest(signature, timestamp, nonce, echostr)
      every { wxpaService.verifyServerConfiguration(expectedRequest) } returns echostr

      // When
      val result = wechatPublicAccountApi.verifyBasicConfig(signature, timestamp, nonce, echostr)

      // Then
      assertEquals(echostr, result)
      verify { wxpaService.verifyServerConfiguration(expectedRequest) }
    }

    @Test
    fun should_throw_exception_when_server_verification_fails() {
      // Given
      val signature = "invalid_signature"
      val timestamp = "1234567890"
      val nonce = "test_nonce"
      val echostr = "test_echo_string"
      val expectedRequest = WxpaService.ServerVerificationRequest(signature, timestamp, nonce, echostr)
      every { wxpaService.verifyServerConfiguration(expectedRequest) } returns null

      // When & Then
      try {
        wechatPublicAccountApi.verifyBasicConfig(signature, timestamp, nonce, echostr)
        assert(false) { "应该抛出异常" }
      } catch (e: IllegalStateException) {
        assertEquals("服务器验证失败", e.message)
      }
      verify { wxpaService.verifyServerConfiguration(expectedRequest) }
    }
  }

  @Nested
  inner class TokenStatusTests {

    @Test
    fun should_successfully_get_token_status() {
      // Given
      val expectedStatus = mapOf(
        "hasAccessToken" to true,
        "accessTokenExpired" to false,
        "hasJsapiTicket" to true,
        "jsapiTicketExpired" to false
      )
      every { wxpaService.getTokenStatus() } returns expectedStatus

      // When
      val result = wechatPublicAccountApi.getTokenStatus()

      // Then
      assertEquals(expectedStatus.mapValues { it.value.toString() }, result)
      verify { wxpaService.getTokenStatus() }
    }

    @Test
    fun should_successfully_refresh_tokens() {
      // Given
      every { wxpaService.forceRefreshTokens() } returns Unit

      // When
      val result = wechatPublicAccountApi.refreshTokens()

      // Then
      assertEquals("Token刷新成功", result)
      verify { wxpaService.forceRefreshTokens() }
    }
  }

  @Nested
  inner class JsapiSignatureTests {

    @Test
    fun should_successfully_generate_jsapi_signature() {
      // Given
      val testUrl = "https://example.com/test"
      val nonceString = "test_nonce_string"
      val expectedSignature = WxpaSignature(
        appId = "test_app_id",
        timestamp = 1234567890L,
        nonceStr = nonceString,
        signature = "test_signature",
        url = testUrl
      )
      every { wxpaService.generateJsapiSignature(testUrl, nonceString) } returns expectedSignature

      // When
      val result = wechatPublicAccountApi.getJsApiUrlSignature(testUrl, nonceString)

      // Then
      assertNotNull(result)
      assertEquals(expectedSignature.appId, result.appId)
      assertEquals(expectedSignature.signature, result.signature)
      assertEquals(expectedSignature.nonceStr, result.nonceStr)
      verify { wxpaService.generateJsapiSignature(testUrl, nonceString) }
    }

    @Test
    fun should_return_null_when_jsapi_signature_generation_fails() {
      // Given
      val testUrl = "https://example.com/test"
      every { wxpaService.generateJsapiSignature(testUrl, null) } returns null

      // When
      val result = wechatPublicAccountApi.getJsApiUrlSignature(testUrl, null)

      // Then
      assertNull(result)
      verify { wxpaService.generateJsapiSignature(testUrl, null) }
    }
  }
}
