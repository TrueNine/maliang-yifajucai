package com.tnmaster.security.annotations

/**
 * # 需要角色注解
 *
 * 用于标记需要特定角色才能访问的方法或类
 *
 * @author TrueNine
 * @since 2025-01-11
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireRole(
  /**
   * 需要的角色名称
   */
  val value: String,
)
