package com.tnmaster.service

import com.tnmaster.dto.api.ApiPermitVariantView
import com.tnmaster.repositories.IApiRepo
import com.tnmaster.repositories.IPermissionsRepo
import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import io.github.truenine.composeserver.consts.ICacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * # 访问控制管理 API
 *
 * @author TrueNine
 * @since 2025-03-01
 */
@Service
class AclService(
  private val apiRepo: IApiRepo,
  private val roleGroupRepo: IRoleGroupRepo,
  private val roleRepo: IRoleRepo,
  private val permissionsRepo: IPermissionsRepo,
) {

  /**
   * ## 所有需校验的 API
   * - 拥有权限 id
   * - 需要登录
   */
  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::api::all'")
  fun fetchAllPermitVariantApis(): List<ApiPermitVariantView> {
    return apiRepo.findALlPermissionsVariant()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::permissions::names::all'")
  fun fetchAllPermissionsNames(): Set<String> {
    return permissionsRepo.findAllPermissionsNames()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::role::names::all'")
  fun fetchAllRoleNames(): Set<String> {
    return roleRepo.findAllRoleNames()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::roleGroup::names::all'")
  fun fetchAllRoleGroupNames(): Set<String> {
    return roleGroupRepo.findAllRoleGroupNames()
  }
}
