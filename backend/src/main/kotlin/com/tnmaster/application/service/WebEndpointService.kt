package com.tnmaster.application.service

import com.tnmaster.entities.Api
import io.github.truenine.composeserver.consts.ICacheNames
import io.github.truenine.composeserver.enums.HttpMethod
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.slf4j
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.get
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Service
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/** ## web servlet 端点服务 */
@Service
class WebEndpointService(
  private val mappingHandlers: List<RequestMappingHandlerMapping>,
  @param:Qualifier(ICacheNames.IRedis.CACHE_MANAGER) private val cm: CacheManager,
  private val newApiService: ApiService,
) {
  data class WebEndpoint(val uri: String, val method: String, val protocol: String, val summary: String? = null, val description: String? = null)

  private val patternMap: Map<RequestMappingInfo, HandlerMethod>
    get() = mappingHandlers.map { handle -> handle.handlerMethods.map { it.key to it.value } }.flatten().associate { it }

  @Cacheable(
    ICacheNames.W1,
    key = "'webEndpoint:'+#method+#uri+#protocol",
    cacheManager = ICacheNames.IRedis.CACHE_MANAGER, // TODO 改造此缓存管理器为动态获取或创建新的缓存
  )
  fun matchEndpoint(uri: String, method: String, protocol: String): WebEndpoint? {
    val e =
      patternMap.keys
        .find { r ->
          val path = r.pathPatternsCondition?.patterns?.any { it.matches(PathContainer.parsePath(uri)) }
          val m = r.methodsCondition.methods.any { it.name == method.uppercase() }
          path == true && m
        }
        ?.let {
          val swaggerAnnotation = patternMap[it]?.getMethodAnnotation(Operation::class.java)
          WebEndpoint(
            uri = replaceUriToAntPath(it.pathPatternsCondition!!.firstPattern.patternString),
            method = it.methodsCondition.methods.map { method -> method.name.uppercase() }.firstOrNull()!!,
            protocol = protocol,
            summary = swaggerAnnotation?.summary,
            description = swaggerAnnotation?.description,
          )
        }
    return e
  }

  @ACID
  fun initEndpoints() {
    log.trace("初始化所有 webEndpoints")
    val endpoints = getWebMvcMappings()
    val apis =
      endpoints.map {
        Api {
          apiPath = it.uri
          apiMethod = HttpMethod[it.method]
          apiProtocol = it.protocol
          name = it.summary
          doc = it.description
        }
      }
    newApiService.postAllFound(apis)
  }

  @CacheEvict(
    ICacheNames.H3, // TODO 写 Duration 表达式
    key = "'webEndpoints'",
    cacheManager = ICacheNames.IRedis.CACHE_MANAGER, // TODO 改造此缓存管理器为动态获取或创建新的缓存
  )
  fun resetCacheEndpoints() {
    log.trace("重置 webEndpoint 缓存")
    cm[ICacheNames.H3]?.evictIfPresent("webEndpoints")
  }

  private fun getWebMvcMappings(): Set<WebEndpoint> {
    val urls = HashSet<WebEndpoint>()
    mappingHandlers.forEach { h ->
      h.handlerMethods.forEach { (mapInfo, v) ->
        mapInfo.pathPatternsCondition?.firstPattern?.patternString?.also { url ->
          val u = replaceUriToAntPath(url)
          val methods = mapInfo.methodsCondition.methods.map { it.name.uppercase() }.toMutableSet()
          if (methods.isEmpty()) methods += listOf("GET", "POST", "DELETE", "PUT", "PATCH")
          val swaggerOperation = v.method.getAnnotation(Operation::class.java)
          methods.forEach { w ->
            urls += WebEndpoint(uri = u, method = w, protocol = "HTTP/2", summary = swaggerOperation?.summary, description = swaggerOperation?.description)
          }
        }
      }
    }
    log.debug("获取所有 url 路径")
    return urls
  }

  companion object {
    @JvmStatic
    private val log = slf4j<WebEndpointService>()

    @JvmStatic
    private val antUriPathReplaceRegex
      get() = "\\{[^}]*}".toRegex()

    @JvmStatic
    private fun trimUri(uri: String): String {
      return uri.trim().run { if (startsWith("/")) this else "/$this" }.removeSuffix("/").let { it.ifBlank { "/" } }
    }

    @JvmStatic
    fun replaceUriToAntPath(uri: String): String = trimUri(uri).replace(antUriPathReplaceRegex, "*")
  }
}
