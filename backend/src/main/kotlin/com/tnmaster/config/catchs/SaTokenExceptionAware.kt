package com.tnmaster.config.catchs

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.SaTokenContextException
import io.github.truenine.composeserver.ErrorResponseEntity
import io.github.truenine.composeserver.SystemLogger
import io.github.truenine.composeserver.logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class SaTokenExceptionAware {
  companion object {
    @JvmStatic
    private val log = logger<SaTokenExceptionAware>()
  }

  private fun Throwable.err(
    errorBy: io.github.truenine.composeserver.enums.HttpStatus? = io.github.truenine.composeserver.enums.HttpStatus.UNKNOWN,
    code: Int? = errorBy?.code,
    msg: String? = errorBy?.message,
    log: SystemLogger? = Companion.log,
  ): ErrorResponseEntity {
    log?.error("msg: {}", this.message, this)
    return ErrorResponseEntity(errorBy = errorBy ?: io.github.truenine.composeserver.enums.HttpStatus.UNKNOWN, code = code, msg = msg)
  }

  // 上下文未初始化
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(SaTokenContextException::class)
  fun saTokenContextException(ex: SaTokenContextException) = ex.err(io.github.truenine.composeserver.enums.HttpStatus._401)

  // 未登录
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(NotLoginException::class)
  fun notLoginException(ex: NotLoginException) = ex.err(io.github.truenine.composeserver.enums.HttpStatus._401)

  // 无权限
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(NotPermissionException::class)
  fun notPermissionException(ex: NotPermissionException) = ex.err(io.github.truenine.composeserver.enums.HttpStatus._403)
}
