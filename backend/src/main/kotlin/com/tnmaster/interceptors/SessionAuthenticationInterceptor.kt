package com.tnmaster.interceptors

import com.fasterxml.jackson.databind.ObjectMapper
import com.tnmaster.enums.HttpHeaders
import com.tnmaster.security.UserContextHolder
import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Session认证拦截器
 *
 * 基于HTTP头中的session ID进行用户认证，替换SaToken认证机制
 *
 * @author TrueNine
 * @since 2025-01-11
 */
@Configuration
class SessionAuthenticationInterceptor(
  private val sessionService: SessionService,
  private val objectMapper: ObjectMapper,
) : WebMvcConfigurer {

  companion object {
    private val log = logger<SessionAuthenticationInterceptor>()

    /**
     * 不需要认证的路径
     */
    private val EXCLUDE_PATHS = arrayOf(
      "/v2/auth/login/**",
      "/v2/auth/logout/**",
      "/v1/user/wxpa/login/**",
      "/actuator/**",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/error",
      "/favicon.ico",
      "/jimmer.html",
      "/jimmer.yaml",
      "/ts.zip"
    )
  }

  /**
   * 写入错误响应
   */
  private fun writeErrorResponse(response: HttpServletResponse, message: String) {
    response.status = HttpStatus.BAD_REQUEST.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.characterEncoding = "UTF-8"

    // 添加CORS头
    response.setHeader("Access-Control-Allow-Origin", "*")
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")

    val errorResponse = mapOf(
      "errorBy" to "BAD_REQUEST",
      "code" to 400,
      "msg" to message
    )

    try {
      val json = objectMapper.writeValueAsString(errorResponse)
      log.debug("Writing error response: {}", json)
      response.writer.use {
        it.write(json)
        it.flush()
      }
    } catch (e: Exception) {
      log.error("Failed to write error response", e)
    }
  }

  inner class InternalInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
      try {
        // 处理 OPTIONS 预检请求
        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
          response.setHeader("Access-Control-Allow-Origin", "*")
          response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
          response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
          response.setHeader("Access-Control-Max-Age", "3600")
          response.status = HttpStatus.OK.value()
          return true
        }
        
        // 从HTTP头中获取session ID
        val sessionId = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (sessionId.isNullOrBlank()) {
          log.debug("请求未提供session ID: {}", request.requestURI)
          writeErrorResponse(response, "请求未提供session ID")
          return false
        }

        // 验证session并获取用户信息
        val userPrincipal = sessionService.validateSessionAndGetUser(sessionId)

        if (userPrincipal == null) {
          log.debug("无效的session ID: {}, URI: {}", sessionId, request.requestURI)
          writeErrorResponse(response, "无效的session ID")
          return false
        }

        // 设置用户上下文
        UserContextHolder.setCurrentUser(userPrincipal)

        // 尝试刷新session（延长过期时间）
        try {
          val refreshResult = sessionService.refreshSession(sessionId)
          if (!refreshResult) {
            log.warn("刷新session失败: {}", sessionId)
          }
        } catch (e: Exception) {
          log.warn("刷新session时发生异常: {}", sessionId, e)
          // 刷新失败不影响当前请求的处理
        }

        log.debug("用户认证成功: account={}, URI={}", userPrincipal.account, request.requestURI)
        return true

      } catch (e: Exception) {
        log.error("认证过程中发生异常: URI={}", request.requestURI, e)
        writeErrorResponse(response, "认证过程中发生异常")
        return false
      }
    }

    override fun afterCompletion(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Any,
      ex: Exception?,
    ) {
      // 清理用户上下文
      UserContextHolder.clear()

      if (ex != null) {
        log.debug("请求处理完成，存在异常: URI={}", request.requestURI, ex)
      } else {
        log.debug("请求处理完成: URI={}", request.requestURI)
      }
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry
      .addInterceptor(InternalInterceptor())
      .order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，确保在其他拦截器之前执行
      .addPathPatterns("/**")
      .excludePathPatterns(*EXCLUDE_PATHS)

    log.info("SessionAuthenticationInterceptor 已注册，排除路径: {}", EXCLUDE_PATHS.contentToString())
  }
}
