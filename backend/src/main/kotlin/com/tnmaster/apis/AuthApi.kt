package com.tnmaster.apis

import com.tnmaster.dto.useraccount.UserAccountAdminSpec
import com.tnmaster.dto.useraccount.UserAccountAdminView
import com.tnmaster.entities.UserAccount
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.security.annotations.IgnoreAuth
import com.tnmaster.security.annotations.RequireLogin
import com.tnmaster.security.annotations.RequirePermission
import com.tnmaster.service.AuthService
import com.tnmaster.service.UserAuthService
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.rds.fetchPq
import io.github.truenine.composeserver.slf4j
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

/**
 * # 用户认证授权 API
 *
 * 该 API 注重于认证授权，但不涉及直接权限管理
 * - 账号登录登出
 * - 下发 token
 * - 第三方平台登录
 * - 踢人
 *
 * @author TrueNine
 * @since 2025-02-17
 */
@Api
@RestController
@RequestMapping("v2/auth")
class AuthApi(
  private val authService: UserAuthService,
  private val userAccountRepo: IUserAccountRepo,
) {
  companion object {
    @JvmStatic
    private val log = slf4j<AuthApi>()
  }

  /**
   * ## 修改自身密码
   *
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   * @param newPasswordConfirm 新密码确认
   */
  @Api
  @RequireLogin
  @PatchMapping("me/password")
  fun patchMePassword(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestParam oldPassword: String,
    @RequestParam newPassword: String,
    @RequestParam newPasswordConfirm: String,
  ): UserAccount {
    log.trace("patchMePassword called, userId={}, oldPassword=****, newPassword=****, newPasswordConfirm=****", authInfo.userId)
    require(listOf(newPassword, newPasswordConfirm, newPasswordConfirm).all { it.isNotBlank() }) {
      log.debug("patchMePassword failed: password blank, userId={}", authInfo.userId)
      "密码不能为空"
    }
    require(newPassword == newPasswordConfirm) {
      log.debug("patchMePassword failed: new password mismatch, userId={}", authInfo.userId)
      "新密码与确认密码不一致"
    }
    log.trace("patchMePassword verifying old password, userId={}", authInfo.userId)
    authService.verifyBase64PasswordOrThrow(authInfo.account, oldPassword)
    log.trace("patchMePassword updating password, userId={}", authInfo.userId)
    val result = authService.updatePasswordByBase64PasswordOrThrow(authInfo.account, newPassword, oldPassword)
    log.debug("patchMePassword success, userId={}", authInfo.userId)
    return result
  }

  /**
   * ## 微信公众号 code 一键登录
   * > TODO 接口防抖，防止频繁重复登录
   *
   * @param code 微信公众号 code
   */
  @Api
  @IgnoreAuth
  @PostMapping("login/wxpa_fast_login_code")
  fun loginByWxpaCode(@RequestParam code: String, @ApiIgnore request: HttpServletRequest, @ApiIgnore auth: AuthRequestInfo?): AuthService.AuthTokenView? {
    log.trace("loginByWxpaCode called, code={}, remoteAddr={}, userId={}", code, request.remoteAddr, auth?.userId)
    val result = authService.loginOrRegisteredByWxpaJsApiCodeOrThrow(code, authRequestInfo = auth, request = request)
    log.debug("loginByWxpaCode result, code={}, userId={}, result={}", code, auth?.userId, result != null)
    return result
  }

  /**
   * ## 批量查询账号信息
   * TODO 管理员
   *
   * @param ids 批量用户 id
   */
  @Api
  @RequireLogin
  @GetMapping("user_accounts/ids")
  fun getUserAccountsByIdsAsAdmin(@RequestParam ids: List<RefId>): List<UserAccount> {
    log.trace("getUserAccountsByIdsAsAdmin called, ids={}", ids)
    val result = authService.fetchAllUserAccountByIds(ids.toSet())
    log.debug("getUserAccountsByIdsAsAdmin result, count={}", result.size)
    return result
  }

  /** ## 获取当前登录用户信息 */
  @Api
  @RequireLogin
  @GetMapping("me/auth_request_info")
  fun getCurrentUserAccount(@ApiIgnore auth: AuthRequestInfo): UserAccount? {
    log.trace("getCurrentUserAccount called, userId={}", auth.userId)
    checkNotNull(auth.userId) {
      log.debug("getCurrentUserAccount failed: not logged in")
      "未登录"
    }
    val result = userAccountRepo.findById(auth.userId).getOrNull()
    log.debug("getCurrentUserAccount result, userId={}, found={}", auth.userId, result != null)
    return result
  }

  /**
   * ## 账号密码登录
   *
   * @param dto 账号密码，密码以 base64 编码进行传递
   */
  @Api
  @IgnoreAuth
  @PostMapping("login/account")
  fun loginBySystemAccount(@RequestBody dto: AccountDto, @ApiIgnore request: HttpServletRequest): AuthService.AuthTokenView {
    log.trace("loginBySystemAccount called, account={}, requestIp={}", dto.account, request.remoteAddr)
    checkNotNull(dto.account) {
      log.debug("loginBySystemAccount failed: account is null")
      "账号不能为空"
    }
    checkNotNull(dto.password) {
      log.debug("loginBySystemAccount failed: password is null, account={}", dto.account)
      "密码不能为空"
    }
    return try {
      val result = authService.loginByAccountAndBase64PasswordOrThrow(account = dto.account, base64Password = dto.password, request = request)
      log.debug("loginBySystemAccount success, account={}", dto.account)
      result
    } catch (ex: Exception) {
      log.debug("loginBySystemAccount failed, account={}, error={}", dto.account, ex.message)
      throw ex
    }
  }

  /** ## 当前 session 退出登录 */
  @Api
  @RequireLogin
  @PostMapping("logout")
  fun logoutByAccount(@ApiIgnore auth: AuthRequestInfo? = null, @ApiIgnore request: HttpServletRequest) {
    log.trace("logoutByAccount called, userId={}, remoteAddr={}", auth?.userId, request.remoteAddr)
    authService.setCurrentSessionLogout(auth, request)
    log.debug("logoutByAccount success, userId={}", auth?.userId)
  }

  /** ## 批量查询账号信息 */
  @Api
  @RequirePermission("user:admin")
  @GetMapping("user_accounts")
  fun getUserAccountsAsAdmin(spec: UserAccountAdminSpec): IPage<UserAccountAdminView> {
    log.trace("getUserAccountsAsAdmin called, spec={}", spec)
    val result = userAccountRepo.sql
      .createQuery(UserAccount::class) {
        where(spec)
        select(table.fetch(UserAccountAdminView::class))
      }
      .fetchPq(spec)
    log.debug("getUserAccountsAsAdmin result, total={}", result.t)
    return result
  }

  /**
   * # 账号密码
   *
   * @param account 账号
   * @param password 密码，base64 编码
   */
  data class AccountDto(val account: String?, val password: String?)
}
