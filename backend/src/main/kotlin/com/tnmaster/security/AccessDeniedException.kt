package com.tnmaster.security

/**
 * 访问拒绝异常
 *
 * 当用户没有足够权限访问资源时抛出
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class AccessDeniedException(message: String) : RuntimeException(message)
