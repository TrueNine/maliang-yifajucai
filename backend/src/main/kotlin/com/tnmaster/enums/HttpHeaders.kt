package com.tnmaster.enums

/**
 * HTTP头常量定义
 *
 * @author TrueNine
 * @since 2025-01-10
 */
object HttpHeaders {
  /**
   * 认证头 - 用于传递session ID
   */
  const val AUTHORIZATION = "Authorization"

  /**
   * 设备ID头 - 用于设备识别
   */
  const val DEVICE_ID = "X-Device-ID"

  /**
   * 用户代理头
   */
  const val USER_AGENT = "User-Agent"

  /**
   * 客户端版本头
   */
  const val CLIENT_VERSION = "X-Client-Version"

  /**
   * 请求追踪ID头
   */
  const val TRACE_ID = "X-Trace-ID"
}
