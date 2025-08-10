package com.tnmaster.interceptors

import com.tnmaster.holders.UserInfoContextHolder
import com.tnmaster.security.UserContextHolder
import com.tnmaster.service.AuthService
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

private val log = slf4j<UserInfoHolderInterceptor>()

@Configuration
class UserInfoHolderInterceptor(private val authService: AuthService) : WebMvcConfigurer {
  init {
    log.trace("register user info holder interceptor")
  }

  inner class InternalInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
      val authInfo = try {
        // 从 UserContextHolder 获取当前用户信息
        val currentUser = UserContextHolder.getCurrentUser()
        if (currentUser != null) {
          AuthRequestInfo(
            account = currentUser.account,
            loginIpAddr = currentUser.loginIpAddr,
            roles = currentUser.roles.toList(),
            permissions = currentUser.permissions.toList(),
            enabled = currentUser.enabled,
            nonLocked = currentUser.enabled,
            nonExpired = currentUser.nonExpired,
            userId = currentUser.userId,
            deviceId = currentUser.deviceId,
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
      return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
      UserInfoContextHolder.close()
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry
      .addInterceptor(InternalInterceptor())
      .order(Ordered.HIGHEST_PRECEDENCE + 1) // 在认证拦截器之后执行
      .addPathPatterns("/**")
      .excludePathPatterns("/v1/user/wxpa/login/**", "/v2/auth/login/**", "/v2/auth/logout/**")
  }
}
