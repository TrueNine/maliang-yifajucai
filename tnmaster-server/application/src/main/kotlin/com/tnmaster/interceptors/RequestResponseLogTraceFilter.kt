package com.tnmaster.interceptors

import com.tnmaster.entities.ApiCallRecord
import com.tnmaster.service.ApiCallRecordService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.servlet.deviceId
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import io.github.truenine.composeserver.slf4j
import org.springframework.web.filter.OncePerRequestFilter

class RequestResponseLogTraceFilter(private val recordService: ApiCallRecordService) : OncePerRequestFilter() {
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val startDatetime = datetime.now()
    doFilter(request, response, filterChain)
    val endDatetime = datetime.now()
    val record = ApiCallRecord {
      reqIp = request.remoteRequestIp
      deviceCode = request.deviceId
      reqPath = request.requestURI
      reqMethod = request.method?.uppercase()
      respCode = response.status
      reqDatetime = startDatetime
      respDatetime = endDatetime
      reqProtocol = request.protocol?.uppercase()
    }
    asyncSaveToCache(record)
  }

  private fun asyncSaveToCache(record: ApiCallRecord) = CoroutineScope(Dispatchers.IO).launch { recordService.postToCache(record) }

  companion object {
    @JvmStatic
    private val log = slf4j<RequestResponseLogTraceFilter>()
  }
}
