package com.tnmaster.controller

import com.tnmaster.service.UserAuthService
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
  inner class `用户信息获取测试` {

    @Test
    fun `应该成功通过code获取用户信息`() {
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
    fun `当获取用户信息失败时应该返回null`() {
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
  inner class `服务器验证测试` {

    @Test
    fun `应该成功验证服务器配置`() {
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
    fun `当服务器验证失败时应该抛出异常`() {
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
  inner class `Token状态测试` {

    @Test
    fun `应该成功获取Token状态`() {
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
    fun `应该成功刷新Token`() {
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
  inner class `JSAPI签名测试` {

    @Test
    fun `应该成功生成JSAPI签名`() {
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
    fun `当生成JSAPI签名失败时应该返回null`() {
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
