package com.tnmaster.controller

import cn.dev33.satoken.annotation.SaIgnore
import io.github.truenine.composeserver.security.oauth2.api.IWxpaWebClient
import io.github.truenine.composeserver.security.oauth2.property.WxpaProperty
import io.github.truenine.composeserver.security.oauth2.service.WxpaService
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
  fun getUserInfoByCode(@RequestParam code: String): IWxpaWebClient.WxpaWebsiteUserInfoResp? {
    return wxpaService.fetchUserInfoByAccessToken(code)
  }

  /** ## 微信消息验证接口 */
  @Api
  @SaIgnore
  @GetMapping("")
  fun verifyBasicConfig(body: WxpaService.WxpaVerifyDto): String {
    return wxpaService.verifyBasicConfigOrThrow(body)
  }

  /** ## 获取 当前服务器的 access_token */
  @Api
  @SaIgnore
  @GetMapping("access_token")
  fun getAccessToken(): String? {
    return wxpaService.wxpaConfigInfo.accessToken
  }

  /** ## 获取票证 */
  @Api
  @SaIgnore
  @GetMapping("js_api_ticket")
  fun getTicket(): String? {
    return wxpaService.wxpaConfigInfo.jsapiTicket
  }

  /** ## 获取配置的 app id */
  @Api
  @SaIgnore
  @GetMapping("app_id")
  fun getAppId(): String? {
    return wxpaService.wxpaConfigInfo.appId
  }

  /** ## 对当前 url 进行签名 */
  @Api
  @SaIgnore
  @GetMapping("js_api_signature")
  fun getJsApiUrlSignature(@RequestParam url: String, @RequestParam(required = false) nonceString: String?): WxpaProperty.WxpaSignatureResp {
    return wxpaService.fetchJsApiSignature(url, nonceString)
  }
}
