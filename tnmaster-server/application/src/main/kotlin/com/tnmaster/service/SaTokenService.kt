package com.tnmaster.service

import cn.dev33.satoken.model.wrapperInfo.SaDisableWrapperInfo
import cn.dev33.satoken.stp.StpUtil
import com.tnmaster.repositories.IUserAccountRepo
import jakarta.servlet.http.HttpServletRequest
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.servlet.deviceId
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SaTokenService(private val userAccountRepo: IUserAccountRepo) {

  /**
   * # 当前登录状态
   *
   * @param getHeaderName 所属头部名称
   * @param token token 值
   * @param login 是否已经登录
   * @param tokenTimeout token 剩余有效期（单位: 秒）
   * @param roles 拥有的角色
   * @param permissions 拥有的权限
   * @param account 账号
   */
  data class SaTokenLoginView(
    val getHeaderName: String,
    val token: String? = null,
    val login: Boolean? = null,
    val tokenTimeout: datetime?,
    val activeTimeout: datetime?,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val account: String,
  )

  /** ## 当前登录 token 信息 */
  val currentSessionTokenInfo: SaTokenLoginView?
    get() =
      StpUtil.getTokenInfo()?.run {
        val timeout = tokenTimeout.let { if (it > 0) datetime.now().plusSeconds(it) else null }
        val activeTimeout = tokenActiveTimeout.let { if (it > 0) datetime.now().plusSeconds(it) else null }
        SaTokenLoginView(
          getHeaderName = this.tokenName,
          login = this.isLogin,
          token = tokenValue,
          tokenTimeout = timeout,
          activeTimeout = activeTimeout,
          account = loginId.toString(),
        )
      }

  /**
   * ## 传入给定值，判断是否被封禁
   * > 正常情况下，传递封禁过期时间即可
   *
   * @param disabledToDatetime 封禁到期时间
   * @param disabled 是否被封禁
   * @param level 封禁等级
   */
  fun toDisabled(
    disabledToDatetime: datetime? = null,
    disabled: Boolean? = disabledToDatetime != null,
    level: Int? = if (disabled == true) 1 else 0,
  ): SaDisableWrapperInfo {
    val dtSeconds = disabledToDatetime?.let { dt -> Duration.between(datetime.now(), dt).abs().seconds }

    return SaDisableWrapperInfo(disabled == true, dtSeconds ?: 0, level ?: 0)
  }

  fun setCurrentSessionToLogin(account: String) {
    StpUtil.login(account)
  }

  /**
   * ## 强制将用户踢出并封禁一段时间
   *
   * @param account 账号
   * @param duration 封禁时长 默认一天
   */
  fun setCurrentSessionToDisabled(account: String, duration: Duration = Duration.ofDays(1)) {
    StpUtil.kickout(account)
    StpUtil.logout(account)
    StpUtil.disable(account, duration.seconds)
  }

  fun setCurrentSessionToLogout() {
    if (StpUtil.isLogin()) {
      StpUtil.logout()
    }
  }

  /**
   * ## 设置登录状态到 sa session
   *
   * @param account 账号
   * @param request servlet 请求上下文
   */
  fun setCurrentSessionLoginState(account: String, request: HttpServletRequest) {
    StpUtil.getSession()["login_account"] = account
    StpUtil.getSession()["login_device_id"] = request.deviceId ?: error { "不明设备进行登录" }
    StpUtil.getSession()["login_remote_request_ip"] = request.remoteRequestIp
    StpUtil.getSession()["login_datetime"] = datetime.now()
    StpUtil.getSession()["login_user_id"] = userAccountRepo.findIdByAccount(account = account)
  }
}
