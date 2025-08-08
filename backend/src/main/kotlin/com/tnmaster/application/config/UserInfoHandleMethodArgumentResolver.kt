package com.tnmaster.application.config

import com.tnmaster.application.holders.UserInfoContextHolder
import io.github.truenine.composeserver.domain.RequestInfo
import io.github.truenine.composeserver.slf4j
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private val log = slf4j<UserInfoHandleMethodArgumentResolver>()

@Component
class UserInfoHandleMethodArgumentResolver : HandlerMethodArgumentResolver, WebMvcConfigurer {
  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    super.addArgumentResolvers(resolvers)
    log.trace("register userinfo argument resolver")
    resolvers.add(this)
  }

  override fun supportsParameter(parameter: MethodParameter): Boolean {
    return RequestInfo::class.java.isAssignableFrom(parameter.parameterType)
  }

  override fun resolveArgument(
    parameter: MethodParameter,
    mavContainer: ModelAndViewContainer?,
    webRequest: NativeWebRequest,
    binderFactory: WebDataBinderFactory?,
  ): Any? {
    return UserInfoContextHolder.get()
  }
}
