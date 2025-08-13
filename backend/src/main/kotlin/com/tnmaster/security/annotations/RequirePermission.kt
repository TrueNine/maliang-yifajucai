package com.tnmaster.security.annotations

/**
 * # 需要权限注解
 *
 * 替换 sa-token 的 @RequirePermission 注解
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
  /**
   * 需要的权限名称
   */
  val value: String,
)
