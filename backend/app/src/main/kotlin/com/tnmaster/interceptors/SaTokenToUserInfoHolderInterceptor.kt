package com.tnmaster.interceptors

import cn.dev33.satoken.`fun`.SaParamFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.stp.StpUtil
import com.tnmaster.holders.UserInfoContextHolder
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.depend.servlet.deviceId
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.slf4j
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private val log = slf4j<SaTokenToUserInfoHolderInterceptor>()

@Configuration
class SaTokenToUserInfoHolderInterceptor : WebMvcConfigurer {
  init {
    log.trace("register sa token interceptor")
  }

  class InternalInterceptor(private val saTokenInterceptorHandler: SaInterceptor = SaInterceptor(), authFunction: SaParamFunction<Any?>) :
    HandlerInterceptor by saTokenInterceptorHandler {
    init {
      saTokenInterceptorHandler.setAuth(authFunction)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
      val authInfo = try {
        if (StpUtil.isLogin()) {
          val account = StpUtil.getSession()["login_account"] as? String
          val enabled = StpUtil.isDisable(account)
          AuthRequestInfo(
            account = account!!,
            loginIpAddr = StpUtil.getSession()["login_remote_request_ip"] as? String,
            roles = StpUtil.getRoleList(),
            permissions = StpUtil.getPermissionList(),
            enabled = enabled,
            nonLocked = !enabled,
            nonExpired = StpUtil.getTokenTimeout() <= 0,
            userId = StpUtil.getSession()["login_user_id"] as RefId,
            deviceId = request.deviceId,
            currentIpAddr = request.remoteRequestIp,
          )
        } else {
          null
        }
      } catch (ex: Exception) {
        log.error("get auth info error", ex)
        null
      }
      UserInfoContextHolder.set(authInfo)
      return saTokenInterceptorHandler.preHandle(request, response, handler)
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
      UserInfoContextHolder.close()
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry
      .addInterceptor(InternalInterceptor {})
      .order(Ordered.HIGHEST_PRECEDENCE)
      .addPathPatterns("/**")
      .excludePathPatterns("/v1/user/wxpa/login/**", "/v2/auth/login/**", "/v2/auth/logout/**")
  }
}
