package com.tnmaster.config

import com.tnmaster.interceptors.RequestResponseLogTraceFilter
import com.tnmaster.service.ApiCallRecordService
import io.github.truenine.composeserver.logger
import io.github.truenine.composeserver.slf4j
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

private val log = logger<FilterRegisterAutoConfiguration>()

@Configuration
class FilterRegisterAutoConfiguration(
  private val apiCallRecordService: ApiCallRecordService
) {
  @Bean
  fun requestResponseLogTraceFilter(): FilterRegistrationBean<RequestResponseLogTraceFilter> {
    val bean = FilterRegistrationBean<RequestResponseLogTraceFilter>()
    bean.filter = RequestResponseLogTraceFilter(apiCallRecordService)
    bean.urlPatterns = listOf("/*")
    bean.order = Ordered.LOWEST_PRECEDENCE
    log.debug("注册 日志记录 并列为首个过滤器 bean = {}", bean)
    return bean
  }
}
