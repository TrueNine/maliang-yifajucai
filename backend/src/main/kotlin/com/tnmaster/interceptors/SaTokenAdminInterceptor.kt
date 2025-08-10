package com.tnmaster.interceptors

import com.tnmaster.security.UserContextHolder
import com.tnmaster.service.AclService
import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.consts.IHeaders
import io.github.truenine.composeserver.consts.IMethods
import io.github.truenine.composeserver.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.casbin.jcasbin.main.Enforcer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ConditionalOnProperty(name = ["app.interceptors.casbin-admin.enabled"], havingValue = "true", matchIfMissing = true)
class CasbinAdminInterceptor(
  private val aclService: AclService,
  private val sessionService: SessionService,
  private val enforcer: Enforcer,
) : WebMvcConfigurer {
  companion object {
    private val log = logger<CasbinAdminInterceptor>()
  }

  /**
   * 认证和权限检查拦截器
   */
  inner class AuthInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
      try {
        val requestPath = request.requestURI
        val requestMethod = request.method

        log.debug("处理请求: {} {}", requestMethod, requestPath)

        val sessionId = request.getHeader(IHeaders.AUTHORIZATION)
        if (sessionId.isNullOrBlank()) {
          // 检查是否需要登录
          if (requiresAuthentication(requestPath, requestMethod)) {
            log.warn("请求 {} {} 需要认证但未提供 sessionId", requestMethod, requestPath)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
          }
          return true
        }

        // 验证 sessionId 并获取用户信息
        val user = sessionService.validateSessionAndGetUser(sessionId)
        if (user == null) {
          log.warn("无效的 sessionId: {}", sessionId.take(10) + "...")
          response.status = HttpServletResponse.SC_UNAUTHORIZED
          return false
        }

        // 刷新会话过期时间
        sessionService.refreshSession(sessionId)

        // 设置用户上下文
        UserContextHolder.setCurrentUser(user)

        // 检查权限
        if (!checkPermission(user.account, requestPath, requestMethod)) {
          log.warn("用户 {} 没有权限访问 {} {}", user.account, requestMethod, requestPath)
          response.status = HttpServletResponse.SC_FORBIDDEN
          return false
        }

        log.debug("用户 {} 成功通过权限检查", user.account)
        return true

      } catch (e: Exception) {
        log.error("权限检查过程中发生错误", e)
        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        return false
      }
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
      // 清除用户上下文
      UserContextHolder.clear()
    }

    /**
     * 检查请求是否需要认证
     */
    private fun requiresAuthentication(path: String, method: String): Boolean {
      val apis = aclService.fetchAllPermitVariantApis()
      return apis.any { api ->
        pathMatches(path, api.apiPath) &&
          methodMatches(method, api.apiMethod?.value) &&
          api.requireLogin == true
      }
    }

    /**
     * 使用 Casbin 检查权限
     */
    private fun checkPermission(account: String, path: String, method: String): Boolean {
      val apis = aclService.fetchAllPermitVariantApis()

      // 查找匹配的 API
      val matchedApi = apis.find { api ->
        pathMatches(path, api.apiPath) && methodMatches(method, api.apiMethod?.value)
      }

      if (matchedApi == null) {
        // 如果没有配置权限要求，默认允许
        return true
      }

      // 如果不需要登录，直接允许
      if (matchedApi.requireLogin != true) {
        return true
      }

      // 如果没有特定权限要求，只要登录即可
      val permissionName = matchedApi.permissions?.name
      if (permissionName == null) {
        return true
      }

      // 使用 Casbin 检查权限
      return enforcer.enforce(account, permissionName, "allow")
    }

    /**
     * 路径匹配（简单实现，可以根据需要扩展为支持通配符）
     */
    private fun pathMatches(requestPath: String, apiPath: String?): Boolean {
      if (apiPath == null) return false
      return requestPath == apiPath || requestPath.startsWith(apiPath)
    }

    /**
     * HTTP 方法匹配
     */
    private fun methodMatches(requestMethod: String, apiMethod: String?): Boolean {
      if (apiMethod == null) return true
      return requestMethod.equals(apiMethod, ignoreCase = true)
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    super.addInterceptors(registry)
    registry
      .addInterceptor(AuthInterceptor())
      .addPathPatterns("/**")
      .excludePathPatterns(
        "/error",
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/jimmer.html",
        "/jimmer.yaml",
        "/ts.zip"
      )
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    registry
      .addMapping("/**")
      .allowedOriginPatterns("*")
      .allowedMethods(*IMethods.all())
      .allowedHeaders("*")
      .exposedHeaders("*")
      .allowCredentials(true)
      .maxAge(3600)
  }
}
