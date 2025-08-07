package com.tnmaster.config.catchs

import io.github.truenine.composeserver.ErrorResponseEntity
import io.github.truenine.composeserver.SystemLogger
import io.github.truenine.composeserver.slf4j
import io.github.truenine.composeserver.typing.HttpStatusTyping
import org.babyfish.jimmer.sql.exception.SaveException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class JimmerExceptionAware(
  @param:Value("\${spring.profiles.active}")
  private val profile: String,
) {
  companion object {
    @JvmStatic
    private val log = slf4j<JimmerExceptionAware>()
  }

  private fun Throwable.err(
    errorBy: HttpStatusTyping? = HttpStatusTyping.UNKNOWN,
    code: Int? = errorBy?.code,
    msg: String? = errorBy?.message,
    alt: String? = errorBy?.alert,
    log: SystemLogger? = Companion.log,
  ): ErrorResponseEntity {
    log?.error("msg: {}", this.message, this)
    return ErrorResponseEntity(errorBy = errorBy ?: HttpStatusTyping.UNKNOWN, code = code, msg = msg, alt = alt)
  }

  // 兜底 save 异常
  @ExceptionHandler(SaveException::class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  fun saveException(ex: SaveException): ErrorResponseEntity {
    return ex.err(HttpStatusTyping.UNKNOWN)
  }

  // 保存时唯一性校验失败
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(SaveException.NotUnique::class)
  fun saveExceptionBeNotUnique(ex: SaveException.NotUnique) = ex.err(HttpStatusTyping._400)
}
