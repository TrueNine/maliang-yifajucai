package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckLogin
import com.tnmaster.entities.UserAccount
import com.tnmaster.service.UserAccountService
import io.github.truenine.composeserver.domain.AuthRequestInfo
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * ## 账号管理 API 第 2 版
 *
 * 该接口注重于直接的账号本身管理
 *
 * @author TrueNine
 * @since 2025-03-06
 */
@Api
@RestController
@RequestMapping("v2/account")
class AccountApi(
  private val accountService: UserAccountService,
) {

  /**
   * ## 更新当前用户昵称
   * > 通过认证信息获取用户ID，更新对应用户的昵称
   *
   * 该接口需要用户登录状态，使用PATCH方法更新用户昵称
   *
   * @param newNickName 新的昵称，通过请求参数传递
   * @param authInfo 认证信息，包含当前用户ID，通过`@ApiIgnore`隐藏
   * @return 更新后的用户账户信息
   */
  @Api
  @SaCheckLogin
  @PatchMapping("me/nick_name")
  fun patchNickNameAsMe(@RequestParam newNickName: String, @ApiIgnore authInfo: AuthRequestInfo): UserAccount {
    // 通过认证信息获取用户ID，并调用服务层更新昵称
    return authInfo.userId.let { accountService.persistNickNameById(it, newNickName) }
  }
}
