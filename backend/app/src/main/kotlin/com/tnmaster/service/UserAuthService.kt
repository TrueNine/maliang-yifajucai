package com.tnmaster.service

import com.tnmaster.dto.userinfo.UserInfoAdminAssignAccountView
import com.tnmaster.dto.userinfo.UserInfoAdminPostDto
import com.tnmaster.entities.*
import com.tnmaster.holders.UserInfoContextHolder
import com.tnmaster.repositories.IDisInfoRepo
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.repositories.IUserInfoRepo
import io.github.truenine.composeserver.*
import io.github.truenine.composeserver.consts.ICacheNames
import io.github.truenine.composeserver.consts.IDbNames
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.toFetcher
import io.github.truenine.composeserver.security.crypto.base64Decode
import io.github.truenine.composeserver.security.crypto.uuid
import io.github.truenine.composeserver.psdk.wxpa.service.WxpaService
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.table.isNotNull
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

@Service
class UserAuthService(
  private val userAccountRepo: IUserAccountRepo,
  private val userInfoRepo: IUserInfoRepo,
  private val passwordEncoder: PasswordEncoder,
  private val disInfoRepo: IDisInfoRepo,
  private val saService: SaTokenService,
  private val wxpaService: WxpaService,
) {
  companion object {
    @JvmStatic
    private val log = slf4j<UserAuthService>()
  }

  /**
   * ## 通过微信公众号 JS API 的 code 进行登录或注册
   * > 该函数用于处理微信公众号 JS API 的 code，进行用户登录或注册操作。
   *
   * 该函数首先通过 `jsApiCode` 获取用户信息，然后根据用户信息判断是否已注册。 如果已注册，则直接登录；如果未注册，则先注册新用户，再进行登录。
   *
   * @param jsApiCode 微信公众号 JS API 的 code，用于获取用户信息
   * @param authRequestInfo 认证请求信息，可选参数
   * @param request HTTP 请求对象，用于处理登录相关的请求信息
   * @return 返回登录后的 `SaTokenLoginView` 对象，如果获取用户信息失败则返回 `null`
   * @throws IllegalStateException 如果公众号注册失败，抛出异常
   */
  @ACID
  fun loginOrRegisteredByWxpaJsApiCodeOrThrow(
    jsApiCode: String,
    authRequestInfo: AuthRequestInfo? = null,
    request: HttpServletRequest,
  ): SaTokenService.SaTokenLoginView? {
    log.trace("loginOrRegisteredByWxpaJsApiCodeOrThrow called, jsApiCode={}, authRequestInfo={}, remoteAddr={}", jsApiCode, authRequestInfo, request.remoteAddr)
    val userInfoResp = wxpaService.getUserInfoByAuthCode(jsApiCode)
    log.trace("userInfoResp: {}", userInfoResp)
    if (null == userInfoResp) {
      log.debug("loginOrRegisteredByWxpaJsApiCodeOrThrow failed: userInfoResp is null, jsApiCode={}", jsApiCode)
      return null
    }
    val registeredUserAccount = fetchRegisteredAccountByWxpaOpenId(userInfoResp.openId)
    log.trace("registeredUserAccount: {}", registeredUserAccount)
    if (null != registeredUserAccount) {
      log.debug("loginOrRegisteredByWxpaJsApiCodeOrThrow: user already registered, openId={}", userInfoResp.openId)
      return loginByAccountOrThrow(registeredUserAccount.account, request, authRequestInfo)
    } else {
      log.debug("loginOrRegisteredByWxpaJsApiCodeOrThrow: registering new user, openId={}", userInfoResp.openId)
      val newAssignAccount = registerAccountByWxpaOpenId(openId = userInfoResp.openId, wechatNickName = userInfoResp.nickname ?: "微信用户")
      checkNotNull(newAssignAccount) {
        log.debug("公众号注册失败, openId={}", userInfoResp.openId)
        "公众号注册失败"
      }
      return loginByAccountOrThrow(newAssignAccount.account!!.account, request, authRequestInfo)
    }
  }

  /**
   * ## 注销登录当前登录账号
   *
   * @param authInfo 授权信息
   * @param request 请求
   */
  fun setCurrentSessionLogout(authInfo: AuthRequestInfo? = null, request: HttpServletRequest) {
    log.trace("setCurrentSessionLogout called, userId={}, remoteAddr={}", authInfo?.userId, request.remoteAddr)
    saService.setCurrentSessionToLogout()
    log.debug("setCurrentSessionLogout success, userId={}", authInfo?.userId)
  }

  /**
   * ## 直接设置账号为登录状态
   *
   * @param account 账号
   * @param request 请求
   * @param authRequestInfo 已登录信息
   */
  @ACID
  internal fun loginByAccountOrThrow(account: String, request: HttpServletRequest, authRequestInfo: AuthRequestInfo? = null): SaTokenService.SaTokenLoginView {
    log.trace("loginByAccountOrThrow called, account={}, remoteAddr={}, isLogin={}", account, request.remoteAddr, authRequestInfo?.isLogin)
    if (authRequestInfo?.isLogin == true) {
      log.debug("loginByAccountOrThrow: already logged in, account={}", account)
      return fetchCurrentLoginInfo()!!
    }
    require(account.isNotBlank()) {
      log.debug("loginByAccountOrThrow failed: account is blank")
      "account is blank"
    }
    saService.setCurrentSessionToLogin(account)
    ifAccountBanned(account) {
      log.debug("loginByAccountOrThrow: account banned, account={}, duration={}s", account, it.seconds)
      saService.setCurrentSessionToDisabled(account)
      error { "账号已被封禁，请于 " + it.seconds + " 后重试" }
    }
    saService.setCurrentSessionLoginState(account, request)
    userAccountRepo.updateLastLoginTimeToNowByAccount(account)
    val result = fetchCurrentLoginInfo()!!.copy(roles = fetchRoleNamesByAccount(account), permissions = fetchPermissionsNamesByAccount(account))
    log.debug("loginByAccountOrThrow success, account={}, roles={}, permissions={}", account, result.roles, result.permissions)
    return result
  }

  /** ## 获取当前登录的信息 */
  fun fetchCurrentLoginInfo(): SaTokenService.SaTokenLoginView? {
    log.trace("fetchCurrentLoginInfo called")
    val result = saService.currentSessionTokenInfo
    log.debug("fetchCurrentLoginInfo result: {}", result)
    return result
  }

  /** ## 获取当前登录的用户 */
  fun fetchCurrentAuthRequestInfo(): AuthRequestInfo? {
    log.trace("fetchCurrentAuthRequestInfo called")
    val result = UserInfoContextHolder.get()
    log.debug("fetchCurrentAuthRequestInfo result: {}", result)
    return result
  }

  /** ## 获取当前登录的用户信息 id */
  fun fetchCurrentUserInfoId(): RefId? {
    log.trace("fetchCurrentUserInfoId called")
    val auth = fetchCurrentAuthRequestInfo()
    val result = auth?.let { auth -> auth.userId.let { userAccountId -> userInfoRepo.findFirstIdByUserAccountId(userAccountId) } }
    log.debug("fetchCurrentUserInfoId result: {}", result)
    return result
  }

  /** ## 获取当前登录的用户账号 */
  fun fetchCurrentUserAccount(): UserAccount? {
    log.trace("fetchCurrentUserAccount called")
    val auth = fetchCurrentAuthRequestInfo()
    val result = auth?.let { auth ->
      auth.userId.let { userAccountId ->
        userAccountRepo.findById(
          userAccountId,
          newFetcher(UserAccount::class).by {
            allScalarFields()
            roleGroups {
              name()
              roles {
                name()
                permissions { name() }
              }
            }
            userInfo { allScalarFields() }
          },
        ).getOrNull()
      }
    }
    log.debug("fetchCurrentUserAccount result: {}", result)
    return result
  }

  /** ## 获取当前登录用户的用户信息 */
  fun fetchCurrentUserInfo(): UserInfo? {
    log.trace("fetchCurrentUserInfo called")
    val auth = UserInfoContextHolder.get()
    val result = auth?.let { auth ->
      auth.userId.let { userAccountId ->
        userInfoRepo.findFirstByUserAccountIdOrNull(
          userAccountId,
          newFetcher(UserInfo::class).by {
            allScalarFields()
            account { allScalarFields() }
          },
        )
      }
    }
    log.debug("fetchCurrentUserInfo result: {}", result)
    return result
  }

  /**
   * ## 账号登录
   *
   * @param account 账号
   * @param base64Password base64 加密后的密码
   * @param request 请求
   * @return 登录信息
   */
  fun loginByAccountAndBase64PasswordOrThrow(account: String, base64Password: String, request: HttpServletRequest): SaTokenService.SaTokenLoginView {
    log.trace("loginByAccountAndBase64PasswordOrThrow called, account={}, base64Password=****, remoteAddr={}", account, request.remoteAddr)
    require(account.isNotBlank()) {
      log.debug("loginByAccountAndBase64PasswordOrThrow failed: account is blank")
      "account is blank"
    }
    require(base64Password.isNotBlank()) {
      log.debug("loginByAccountAndBase64PasswordOrThrow failed: base64Password is blank, account={}", account)
      "base64Password is blank"
    }
    verifyBase64PasswordOrThrow(account, base64Password)
    log.debug("Password verified for account={}", account)
    return loginByAccountOrThrow(account, request)
  }

  /**
   * ## 分配一个系统内部账号
   *
   * @param account 账号
   * @param password 密码
   * @param nickName 昵称
   * @param createUserAccountId 创建账号的用户 id
   * @param baseUserInfo 基础用户信息
   */
  @ACID
  fun assignSystemAccount(
    account: String = uuid(),
    password: String = uuid(),
    nickName: String = account,
    createUserAccountId: RefId = IDbNames.Rbac.ROOT_ID,
    baseUserInfo: UserInfoAdminPostDto? = null,
  ): UserInfo {
    log.trace(
      "assignSystemAccount called, account={}, nickName={}, createUserAccountId={}, baseUserInfo={}",
      account,
      nickName,
      createUserAccountId,
      baseUserInfo
    )
    require(account.isNotBlank()) {
      log.debug("assignSystemAccount failed: account is blank")
      "account is blank"
    }
    require(password.isNotBlank()) {
      log.debug("assignSystemAccount failed: password is blank")
      "password is blank"
    }
    require(nickName.isNotBlank()) {
      log.debug("assignSystemAccount failed: nickName is blank")
      "nickName is blank"
    }
    require(createUserAccountId.isId()) {
      log.debug("assignSystemAccount failed: createUserAccountId is blank")
      "createUserAccountId is blank"
    }
    baseUserInfo?.also {
      it.phone?.also {
        log.trace("assignSystemAccount checking phone duplicate: {}", it)
        check(!userInfoRepo.existsByPhone(it)) {
          log.debug("手机号已被占用: {}", it)
          "手机号 $it 已被占用"
        }
      }
      it.idCard?.also {
        log.trace("assignSystemAccount checking idCard duplicate: {}", it)
        check(!userInfoRepo.existsByIdCard(it)) {
          log.debug("身份证号已被占用: {}", it)
          "身份证号 $it 已被占用"
        }
      }
      it.email?.also {
        log.trace("assignSystemAccount checking email duplicate: {}", it)
        check(!userInfoRepo.existsByEmail(it)) {
          log.debug("邮箱已被占用: {}", it)
          "邮箱 $it 已被占用"
        }
      }
      it.disInfo?.certCode?.also {
        log.trace("assignSystemAccount checking certCode duplicate: {}", it)
        check(!disInfoRepo.existsByCertCode(it)) {
          log.debug("残疾证号已被占用: {}", it)
          "残疾证号 $it 已被占用"
        }
      }
    }
    log.trace("assigning account: {} , nickName: {} , createUserAccountId: {}", account, nickName, createUserAccountId)
    val assignedAccountUserInfo = userInfoRepo.save(
      UserInfo(base = baseUserInfo?.toEntity()) {
        this.createUserId = createUserAccountId
        pri = true
        account {
          this.account = account
          this.createUserId = createUserAccountId
          roleGroups = listOf(RoleGroup { id = IDbNames.Rbac.USER_ID })
          this.nickName = nickName
          pwdEnc = passwordEncoder.encode(password)
          createUserId = createUserAccountId
        }
      },
      SaveMode.INSERT_ONLY,
      AssociatedSaveMode.REPLACE
    )
    log.trace("assign system account info: {}", assignedAccountUserInfo)
    return assignedAccountUserInfo
  }

  /**
   * ## 微信公众号 直接 openId 注册
   * > 警告，此方法未对 openId 作出任何校验
   *
   * @param openId 微信公众号 openId
   * @param wechatNickName 微信呢称
   */
  @ACID
  fun registerAccountByWxpaOpenId(openId: String, wechatNickName: String): UserInfo? {
    log.trace("registerAccountByWxpaOpenId called, openId={}, wechatNickName={}", openId, wechatNickName)
    val foundOpenId = userInfoRepo.existsByWechatOpenId(openId)
    log.trace("registerAccountByWxpaOpenId foundOpenId: {}", foundOpenId)
    if (foundOpenId) {
      val foundOpenIdUserInfo =
        userInfoRepo.findFirstByWechatOpenId(openId, UserInfoAdminAssignAccountView::class.toFetcher()) ?: error {
          log.debug("openId is not found: {}", openId)
          "openId is not found"
        }
      log.debug("registerAccountByWxpaOpenId: foundOpenIdUserInfo={}", foundOpenIdUserInfo)
      return foundOpenIdUserInfo
    } else {
      val assigned = assignSystemAccount(nickName = wechatNickName, createUserAccountId = IDbNames.Rbac.ROOT_ID)
      log.trace("registerAccountByWxpaOpenId assigned: {}", assigned)
      userInfoRepo.save(
        UserInfo {
          id = assigned.id
          wechatOpenid = openId
        }, SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE
      )
      val result = userInfoRepo.findById(assigned.id, UserInfoAdminAssignAccountView::class.toFetcher()).getOrNull()
      log.debug("registerAccountByWxpaOpenId: new user result={}", result)
      return result
    }
  }

  /** ## 请求已注册的微信公众号账号 */
  fun fetchRegisteredAccountByWxpaOpenId(openId: String): UserAccount? {
    log.trace("fetchRegisteredAccountByWxpaOpenId called, openId={}", openId)
    val result = userInfoRepo.sql.createQuery(UserInfo::class) {
      where(table.wechatOpenid eq openId, table.account.isNotNull())
      select(table.account)
    }.limit(1).offset(0).fetchOneOrNull()
    log.debug("fetchRegisteredAccountByWxpaOpenId result: {}", result)
    return result
  }

  /** ## 判断账号是否注册了微信公众号 */
  fun foundRegisteredByWxpaOpenId(openId: String): Boolean {
    log.trace("foundRegisteredByWxpaOpenId called, openId={}", openId)
    val result = userInfoRepo.sql.createQuery(UserInfo::class) {
      where(table.wechatOpenid eq openId, table.userAccountId.isNotNull())
      select(table.id)
    }.exists()
    log.debug("foundRegisteredByWxpaOpenId result: {}", result)
    return result
  }

  /** ## 批量 id 查询批量账号 */
  fun fetchAllUserAccountByIds(ids: Set<RefId>): List<UserAccount> {
    log.trace("fetchAllUserAccountByIds called, ids={}", ids)
    if (ids.isEmpty()) {
      log.debug("fetchAllUserAccountByIds: empty ids")
      return emptyList()
    }
    val result = userAccountRepo.findAllById(ids)
    log.debug("fetchAllUserAccountByIds result, count={}", result.size)
    return result
  }

  /** 根据账号查询 user id */
  fun fetchIdByAccount(account: String): RefId? {
    log.trace("fetchIdByAccount called, account={}", account)
    val result = userAccountRepo.findIdByAccount(account)
    log.debug("fetchIdByAccount result: {}", result)
    return result
  }

  /**
   * ## 直接解除用户封禁状态
   *
   * @param account 账号
   */
  @ACID
  fun unbanAccountByAccount(account: String) {
    log.trace("unbanAccountByAccount called, account={}", account)
    userAccountRepo.update(
      UserAccount {
        this.account = account
        banTime = null
      })
    log.debug("unbanAccountByAccount success, account={}", account)
  }

  /** TODO 需要优化，查询与设置耦合 */
  @ACID
  fun fetchAccountIsBandByAccount(account: String): Boolean {
    log.trace("fetchAccountIsBandByAccount called, account={}", account)
    val banTime = fetchBanTimeByAccount(account)
    val nonSetBanTime = null == banTime
    if (nonSetBanTime) {
      log.debug("fetchAccountIsBandByAccount: no banTime, account={}", account)
      return false
    }
    val isNotExpireBanTime = banTime?.isAfter(datetime.now()) == true
    if (isNotExpireBanTime) {
      log.debug("fetchAccountIsBandByAccount: ban not expired, account={}, banTime={}", account, banTime)
      return true
    }
    unbanAccountByAccount(account)
    log.debug("fetchAccountIsBandByAccount: ban expired, account={}", account)
    return false
  }

  @ACID
  fun ifAccountBanned(account: String, block: (duration: Duration) -> Unit = {}): Boolean {
    log.trace("ifAccountBanned called, account={}", account)
    val isBand = fetchAccountIsBandByAccount(account)
    if (isBand) {
      val banTime = fetchBanTimeByAccount(account)!!
      val duration = Duration.between(datetime.now(), banTime)
      log.debug("ifAccountBanned: account banned, account={}, duration={}s", account, duration.seconds)
      block(duration)
      return true
    }
    log.debug("ifAccountBanned: account not banned, account={}", account)
    return false
  }

  fun fetchBanTimeByAccount(account: String): datetime? {
    log.trace("fetchBanTimeByAccount called, account={}", account)
    val result = userAccountRepo.findDisabledInfoByAccount(account)?.banTime
    log.debug("fetchBanTimeByAccount result: {}", result)
    return result
  }

  /**
   * ## 验证账号密码 base64
   *
   * @param account 账号
   * @param base64Password base64 密码
   */
  fun verifyBase64Password(account: String?, base64Password: String?): Boolean {
    log.trace("verifyBase64Password called, account={}, base64Password=****", account)
    check(account?.isNotBlank() == true) {
      log.debug("verifyBase64Password failed: account blank")
      "账号不能为空"
    }
    check(base64Password?.isNotBlank() == true) {
      log.debug("verifyBase64Password failed: password blank, account={}", account)
      "密码不能为空"
    }
    val password = base64Password.base64Decode(Charsets.UTF_8)
    val result = verifyPassword(account, password)
    log.debug("verifyBase64Password result, account={}, result={}", account, result)
    return result
  }

  fun verifyBase64PasswordOrThrow(account: String, base64Password: String): Boolean {
    log.trace("verifyBase64PasswordOrThrow called, account={}, base64Password=****", account)
    val result = verifyPasswordOrThrow(account, base64Password.base64Decode(Charsets.UTF_8))
    log.debug("verifyBase64PasswordOrThrow result, account={}, result={}", account, result)
    return result
  }

  fun verifyPassword(account: String, password: String): Boolean {
    log.trace("verifyPassword called, account={}, password=******", account)
    val pwdEnc = userAccountRepo.findPwdEncByAccount(account) ?: error("account $account not found")
    log.trace("verifyPassword got pwdEnc, account={}, pwdEnc=****", account)
    val match = passwordEncoder.matches(password, pwdEnc)
    log.debug("verifyPassword match result, account={}, match={}", account, match)
    return match
  }

  fun verifyPasswordOrThrow(account: String, password: String): Boolean {
    log.trace("verifyPasswordOrThrow called, account={}, password=******", account)
    val result = verifyPassword(account, password)
    log.debug("verifyPasswordOrThrow result for account={}, result={}", account, result)
    require(result) {
      log.debug("verifyPasswordOrThrow failed: account or password error, account={}", account)
      "账号或密码错误"
    }
    return result
  }

  @Cacheable(
    ICacheNames.M10,
    cacheManager = ICacheNames.IRedis.CACHE_MANAGER,
    key = "'auth:permissionsNames:'+#account",
    condition = "#account != null",
    unless = "#result == null",
  )
  fun fetchPermissionsNamesByAccount(account: String): Set<String> {
    log.trace("fetchPermissionsNamesByAccount called, account={}", account)
    val result = userAccountRepo.findAllPermissionsNameByAccount(account)
    log.debug("fetchPermissionsNamesByAccount result, account={}, permissions={}", account, result)
    return result
  }

  @Cacheable(
    ICacheNames.M10,
    cacheManager = ICacheNames.IRedis.CACHE_MANAGER,
    key = "'auth:roleNames:'+#account",
    condition = "#account != null",
    unless = "#result == null",
  )
  fun fetchRoleNamesByAccount(account: String): Set<String> {
    log.trace("fetchRoleNamesByAccount called, account={}", account)
    val result = userAccountRepo.findAllRoleNameByAccount(account)
    log.debug("fetchRoleNamesByAccount result, account={}, roles={}", account, result)
    return result
  }

  @Cacheable(
    ICacheNames.H2,
    cacheManager = ICacheNames.IRedis.CACHE_MANAGER,
    key = "'authRequestInfo:'+#account",
    condition = "#account != null",
    unless = "#result == null",
  )
  fun fetchAuthRequestInfoByAccount(account: String): AuthRequestInfo {
    log.trace("fetchAuthRequestInfoByAccount called, account={}", account)
    val userAccount = userAccountRepo.findUserAccountByAccount(account)
    checkNotNull(userAccount) {
      log.debug("fetchAuthRequestInfoByAccount failed: account not found, account={}", account)
      "账号 $account 不存在"
    }
    val enabled = !userAccount.disabled
    val result = AuthRequestInfo(
      account = userAccount.account,
      userId = userAccount.id,
      enabled = enabled,
      encryptedPassword = userAccount.pwdEnc,
      nonLocked = enabled,
      nonExpired = enabled,
      roles = userAccount.roleGroups.map { it.name }.toList(),
      permissions = userAccount.roleGroups.map { it.roles }.flatten().map { it.permissions }.flatten().map { it.name },
    )
    log.debug("fetchAuthRequestInfoByAccount result, account={}, result={}", account, result)
    return result
  }

  /**
   * ## 更新用户密码
   *
   * @param account 账号
   * @param newPassword 新密码
   * @param oldPassword 旧密码
   */
  fun updatePasswordByBase64PasswordOrThrow(account: String, newPassword: String, oldPassword: String): UserAccount {
    log.trace("updatePasswordByBase64PasswordOrThrow called, account={}, newPassword=****, oldPassword=****", account)
    require(newPassword.isNotBlank()) {
      log.debug("updatePasswordByBase64PasswordOrThrow failed: newPassword blank, account={}", account)
      "新密码不能为空"
    }
    require(oldPassword.isNotBlank()) {
      log.debug("updatePasswordByBase64PasswordOrThrow failed: oldPassword blank, account={}", account)
      "旧密码不能为空"
    }
    require(newPassword != oldPassword) {
      log.debug("updatePasswordByBase64PasswordOrThrow failed: newPassword equals oldPassword, account={}", account)
      "新密码与旧密码不能相同"
    }
    verifyBase64PasswordOrThrow(account, oldPassword)
    val newPwd = passwordEncoder.encode(newPassword.base64Decode())
    val result = userAccountRepo.update(
      UserAccount {
        this.account = account
        pwdEnc = newPwd
      })
    log.debug("updatePasswordByBase64PasswordOrThrow success, account={}", account)
    return result
  }
}
