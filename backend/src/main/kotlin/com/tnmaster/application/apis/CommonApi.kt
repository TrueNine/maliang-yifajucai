package com.tnmaster.application.apis

import cn.dev33.satoken.annotation.SaCheckPermission
import io.github.truenine.composeserver.data.extract.service.IMinoritiesService
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import io.github.truenine.composeserver.generator.IOrderCodeGenerator
import io.github.truenine.composeserver.generator.ISnowflakeGenerator
import io.github.truenine.composeserver.security.crypto.CryptographicOperations
import io.github.truenine.composeserver.security.crypto.domain.IKeysRepo
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/** ## 常用数据/工具接口 */
@Api
@RestController
@RequestMapping("v2/common")
class CommonApi(
  private val minoritiesService: IMinoritiesService,
  private val bizCode: IOrderCodeGenerator,
  private val snowflakeIdGenerator: ISnowflakeGenerator,
  private val keyRepo: IKeysRepo,
) {
  /** ## 用于检测服务器是否存活 */
  @Api
  @GetMapping("ping")
  fun pong() = 1

  /** ## 获取当前用户的 ip 地址 */
  @Api
  @GetMapping("ip")
  fun getCurrentSessionRemoteIpAddr(@ApiIgnore request: HttpServletRequest): String {
    val ip = request.remoteRequestIp
    return ip
  }

  /** ## AES解密 */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("ass_decrypt")
  fun aseDecrypt(@RequestParam ciphertext: String): String {
    return CryptographicOperations.decryptByAesKey(keyRepo.basicAesKey()!!, ciphertext)!!
  }

  /** ## AES加密 */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("ase_encrypt")
  fun aseEncrypt(@RequestParam plaintext: String): String {
    return CryptographicOperations.encryptByAesKey(keyRepo.basicAesKey()!!, plaintext)!!
  }

  /** ## 获取一个 java 版的 uuid */
  @Api
  @GetMapping("java_uuid")
  fun getUuid(): String {
    return UUID.randomUUID().toString()
  }

  /** ## 雪花算法id */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("server_snowflake_id")
  fun getServerSnowflakeId(): String {
    return snowflakeIdGenerator.nextString()
  }

  /** ## 获取一个业务单号 */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("business_order_code")
  fun getBusinessOrderCode(): String {
    return bizCode.nextString()
  }

  /** ## 所有少数民族 */
  @Api
  @GetMapping("china_minorities")
  fun getAllChinaMinorities(): List<String> {
    return minoritiesService.findAllMinoritiesZ()
  }

  /**
   * ## 获取服务器异常
   * > 这通常用于测试目的
   */
  @Api
  @SaCheckPermission("ROOT")
  @GetMapping("server_throwable")
  fun getServerThrowable() {
    throw RuntimeException("服务器异常")
  }
}
