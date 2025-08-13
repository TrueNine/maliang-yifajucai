package com.tnmaster.security.annotations

/**
 * # 忽略认证注解
 *
 * 替换 sa-token 的 @IgnoreAuth 注解
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreAuth
