package com.tnmaster.application.config.catchs

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.github.truenine.composeserver.ErrorResponseEntity
import io.github.truenine.composeserver.logger
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.http.HttpStatus
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.sql.SQLException
import io.github.truenine.composeserver.enums.HttpStatus as HttpStatusEnum

@RestControllerAdvice
class SpringWebMvcExceptionAware(
  @param:Value("\${spring.profiles.active:default}") val profile: String,
) {
  companion object {
    @JvmStatic
    private val log = logger<SpringWebMvcExceptionAware>()
  }

  /**
   * 判断字符串是否包含中文
   * @return 如果包含中文字符返回true，否则返回false
   */
  fun String.containsChinese(): Boolean {
    return this.any { char ->
      Character.UnicodeBlock.of(char) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
        Character.UnicodeBlock.of(char) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
        Character.UnicodeBlock.of(char) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
    }
  }

  private fun Throwable.err(
    errorBy: HttpStatusEnum? = HttpStatusEnum.UNKNOWN,
    code: Int? = errorBy?.code,
    msg: String? = errorBy?.message,
  ): ErrorResponseEntity {
    if (profile == "dev") {
      log.error("msg: {}", this.message, this)
    } else {
      log.warn("msg: {}", this.message, this)
    }
    return ErrorResponseEntity(errorBy = errorBy ?: HttpStatusEnum.UNKNOWN, code = code, msg = msg)
  }

  /** 兜底异常 */
  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception::class)
  fun exception(ex: Exception) = ex.err()

  // 中文动态错误消息
  @ResponseBody
  @ExceptionHandler(IllegalArgumentException::class)
  fun illegalArgumentException(
    ex: IllegalArgumentException,
    response: HttpServletResponse,
  ): ErrorResponseEntity {
    if (ex.message?.containsChinese() == true) {
      response.status = HttpStatusEnum._400.value
      return ErrorResponseEntity(
        errorBy = HttpStatusEnum._400,
        msg = ex.message,
      )
    } else {
      response.status = HttpStatusEnum.UNKNOWN.value
      log.warn("IllegalArgumentException", ex)
      return ErrorResponseEntity(errorBy = HttpStatusEnum.UNKNOWN)
    }
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MismatchedInputException::class)
  fun mismatchedInputException(ex: MismatchedInputException) = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(org.springframework.transaction.TransactionSystemException::class)
  fun transactionSystemException(ex: org.springframework.transaction.TransactionSystemException): ErrorResponseEntity {
    ex.rootCause?.also { r ->
      if (r is ConstraintViolationException) {
        return constraintViolationException(r)
      }
    }
    return ex.err(HttpStatusEnum._400)
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(java.lang.IllegalStateException::class)
  fun illegalStateException(ex: java.lang.IllegalStateException) = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UninitializedPropertyAccessException::class)
  fun uninitializedPropertyAccessException(ex: UninitializedPropertyAccessException) = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  @ExceptionHandler(NotImplementedError::class)
  fun notImplementedError(ex: NotImplementedError) = ex.err(HttpStatusEnum._501)

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoResourceFoundException::class)
  fun noResourceFoundException(ex: NoResourceFoundException) = ex.err(HttpStatusEnum._404)

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun missingServletRequestParameterException(ex: MissingServletRequestParameterException) = ex.err()

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidDataAccessResourceUsageException::class)
  fun invalidDataAccessResourceUsageException(ex: InvalidDataAccessResourceUsageException) = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DataIntegrityViolationException::class)
  fun dataIntegrityViolationException(ex: DataIntegrityViolationException): ErrorResponseEntity {
    return ex.rootCause?.let { if (it is SQLException) sqlException(it) else null } ?: ex.err()
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException::class)
  fun constraintViolationException(ex: ConstraintViolationException): ErrorResponseEntity = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun methodArgumentNotValidException(ex: MethodArgumentNotValidException) = ex.err(HttpStatusEnum._400)

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(java.lang.NullPointerException::class)
  fun javaNullPointerException(ex: java.lang.NullPointerException) = ex.err()

  @ResponseBody
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun httpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException) = ex.err(HttpStatusEnum._405)

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
  fun httpMediaTypeNotAcceptableException(ex: HttpMediaTypeNotAcceptableException) = ex.err(HttpStatusEnum._406)

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotWritableException::class)
  fun httpMessageNotWritableException(ex: org.springframework.http.converter.HttpMessageNotWritableException) = ex.err()

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(SQLException::class)
  fun sqlException(ex: SQLException): ErrorResponseEntity {
    val state = ex.sqlState
    val errCode = ex.errorCode
    val knownMessage = handleSqlExceptionState(state)
    log.error("STAT: {}, CODE: {}, K_MSG: {}", state, errCode, knownMessage)
    return ex.err()
  }

  fun handleSqlExceptionState(state: String): String {
    return when (state) {
      "08001" -> "连接数据库失败"
      "42883" -> "无类型运算符"
      "22001" -> "存入时，给定的数据超出最大长度"
      "22P02" -> "对于 bigint 类型，输了了无效的字符"
      "23502" -> "插入时违反非空约束"
      "23505" -> "插入时违法唯一约束"
      "25006" -> "无法在只读事务中执行删除语句"
      "42703" -> "未找到指定的数据列"
      else -> "unknown sql state: $state"
    }
  }
}
