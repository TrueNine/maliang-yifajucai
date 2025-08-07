package com.tnmaster.config

import cn.dev33.satoken.model.wrapperInfo.SaDisableWrapperInfo
import cn.dev33.satoken.stp.StpInterface
import com.tnmaster.service.SaTokenService
import com.tnmaster.service.UserAuthService
import org.springframework.stereotype.Component

@Component
class SaTokenPermissionsConfig(
  private val authService: UserAuthService,
  private val saService: SaTokenService,
) : StpInterface {

  /** ## 判断是否被封禁 */
  override fun isDisabled(loginId: Any?, service: String?): SaDisableWrapperInfo? {
    return loginId?.toString()?.let { account ->
      val banTime = authService.fetchBanTimeByAccount(account)
      saService.toDisabled(banTime)
    }
  }

  override fun getPermissionList(p0: Any?, p1: String?): List<String?> {
    if (null == p0) return emptyList()
    if (p0 !is String) return emptyList()
    return authService.fetchPermissionsNamesByAccount(p0).distinct()
  }

  override fun getRoleList(p0: Any?, p1: String?): List<String?> {
    if (null == p0) return emptyList()
    if (p0 !is String) return emptyList()
    return authService.fetchRoleNamesByAccount(p0).toList()
  }
}
