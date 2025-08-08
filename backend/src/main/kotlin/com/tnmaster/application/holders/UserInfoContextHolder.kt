package com.tnmaster.application.holders

import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.holders.AbstractThreadLocalHolder

/** 全局用户信息持有者 */
object UserInfoContextHolder : AbstractThreadLocalHolder<AuthRequestInfo?>()
