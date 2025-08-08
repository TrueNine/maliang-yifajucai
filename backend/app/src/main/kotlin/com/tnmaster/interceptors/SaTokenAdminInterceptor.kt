package com.tnmaster.interceptors

import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.router.SaHttpMethod
import cn.dev33.satoken.router.SaRouter
import cn.dev33.satoken.stp.StpUtil
import com.tnmaster.service.AclService
import io.github.truenine.composeserver.consts.IMethods
import io.github.truenine.composeserver.slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ConditionalOnProperty(name = ["app.interceptors.sa-token-admin.enabled"], havingValue = "true", matchIfMissing = true)
class SaTokenAdminInterceptor(private val aclService: AclService) : WebMvcConfigurer {
  companion object {
    private val log = slf4j<SaTokenAdminInterceptor>()
  }

  fun String?.toSaHttpMethod(): SaHttpMethod {
    return when (this) {
      "GET" -> SaHttpMethod.GET
      "POST" -> SaHttpMethod.POST
      "PUT" -> SaHttpMethod.PUT
      "DELETE" -> SaHttpMethod.DELETE
      "PATCH" -> SaHttpMethod.PATCH
      "OPTIONS" -> SaHttpMethod.OPTIONS
      "HEAD" -> SaHttpMethod.HEAD
      "TRACE" -> SaHttpMethod.TRACE
      "CONNECT" -> SaHttpMethod.CONNECT
      null -> SaHttpMethod.ALL
      else -> SaHttpMethod.ALL
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    super.addInterceptors(registry)
    registry
      .addInterceptor(
        SaInterceptor {
          val apis = aclService.fetchAllPermitVariantApis()
          apis.forEach { apiView ->
            SaRouter.match(apiView.apiMethod?.value?.toSaHttpMethod()).match(apiView.apiPath) { _ ->
              if (apiView.requireLogin == true) {
                StpUtil.checkLogin()
              }
              apiView.permissions?.name?.let { permissionsName -> StpUtil.checkPermission(permissionsName) }
            }
          }
        }
      )
      .addPathPatterns("/**")
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
