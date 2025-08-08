package com.tnmaster.controller

import cn.dev33.satoken.annotation.SaIgnore
import io.github.truenine.composeserver.psdk.wxpa.model.WxpaSignature
import io.github.truenine.composeserver.psdk.wxpa.model.WxpaUserInfo
import io.github.truenine.composeserver.psdk.wxpa.service.WxpaService
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** # 微信公众号接口 */
@Api
@RestController
@RequestMapping("v2/wxpa")
class WechatPublicAccountApi(private val wxpaService: WxpaService) {

  /**
   * ## code 换取用户信息
   *
   * @param code 获取用户信息的 code
   */
  @Api
  @SaIgnore
  @GetMapping("user_info")
  fun getUserInfoByCode(@RequestParam code: String): WxpaUserInfo? {
    return wxpaService.getUserInfoByAuthCode(code)
  }

  /** ## 微信消息验证接口 */
  @Api
  @SaIgnore
  @GetMapping("")
  fun verifyBasicConfig(
    @RequestParam signature: String,
    @RequestParam timestamp: String,
    @RequestParam nonce: String,
    @RequestParam echostr: String,
  ): String? {
    val request = WxpaService.ServerVerificationRequest(signature, timestamp, nonce, echostr)
    return wxpaService.verifyServerConfiguration(request) ?: throw IllegalStateException("服务器验证失败")
  }

  /** ## 获取Token状态信息 */
  @Api
  @SaIgnore
  @GetMapping("token_status")
  fun getTokenStatus(): Map<String, String> {
    val status = wxpaService.getTokenStatus()
    return status.mapValues { it.value.toString() }
  }

  /** ## 强制刷新所有Token */
  @Api
  @SaIgnore
  @GetMapping("refresh_tokens")
  fun refreshTokens(): String {
    wxpaService.forceRefreshTokens()
    return "Token刷新成功"
  }

  /** ## 对当前 url 进行签名 */
  @Api
  @SaIgnore
  @GetMapping("js_api_signature")
  fun getJsApiUrlSignature(@RequestParam url: String, @RequestParam(required = false) nonceString: String?): WxpaSignature? {
    return wxpaService.generateJsapiSignature(url, nonceString)
  }
}
