package com.tnmaster.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import java.io.Serializable
import java.time.Duration

/**
 * 用户会话数据结构
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
data class SessionData @JsonCreator constructor(
  /**
   * 会话ID
   */
  @param:JsonProperty("sessionId")
  val sessionId: String,

  /**
   * 用户账号
   */
  @param:JsonProperty("account")
  val account: String,

  /**
   * 用户ID
   */
  @param:JsonProperty("userId")
  val userId: RefId,

  /**
   * 设备ID
   */
  @param:JsonProperty("deviceId")
  val deviceId: String,

  /**
   * 登录IP地址
   */
  @param:JsonProperty("loginIpAddr")
  val loginIpAddr: String,

  /**
   * 登录时间
   */
  @param:JsonProperty("loginTime")
  @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
  val loginTime: datetime,

  /**
   * 用户角色列表
   */
  @param:JsonProperty("roles")
  val roles: Set<String>,

  /**
   * 用户权限列表
   */
  @param:JsonProperty("permissions")
  val permissions: Set<String>,

  /**
   * 会话过期时间
   */
  @param:JsonProperty("expireTime")
  @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
  val expireTime: datetime,

  /**
   * 是否被禁用
   */
  @param:JsonProperty("disabled")
  val disabled: Boolean = false,

  /**
   * 用户昵称
   */
  @param:JsonProperty("nickName")
  val nickName: String? = null,

  /**
   * 用户代理
   */
  @param:JsonProperty("userAgent")
  val userAgent: String? = null,

  /**
   * 客户端版本
   */
  @param:JsonProperty("clientVersion")
  val clientVersion: String? = null,
) : Serializable {
  /**
   * 检查会话是否已过期
   */
  @JsonIgnore
  fun isExpired(): Boolean = datetime.now().isAfter(expireTime)

  /**
   * 获取会话剩余时间
   */
  @JsonIgnore
  fun getRemainingTime(): Duration = Duration.between(datetime.now(), expireTime)

  /**
   * 检查用户是否有指定角色
   */
  fun hasRole(role: String): Boolean = roles.contains(role)

  /**
   * 检查用户是否有指定权限
   */
  fun hasPermission(permission: String): Boolean = permissions.contains(permission)

  /**
   * 检查用户是否有任意指定角色
   */
  fun hasAnyRole(vararg roles: String): Boolean = roles.any { hasRole(it) }

  /**
   * 检查用户是否有任意指定权限
   */
  fun hasAnyPermission(vararg permissions: String): Boolean = permissions.any { hasPermission(it) }

  /**
   * 检查用户是否有所有指定角色
   */
  fun hasAllRoles(vararg roles: String): Boolean = roles.all { hasRole(it) }

  /**
   * 检查用户是否有所有指定权限
   */
  fun hasAllPermissions(vararg permissions: String): Boolean = permissions.all { hasPermission(it) }
}
