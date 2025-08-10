package com.tnmaster.apis

import com.tnmaster.security.annotations.RequirePermission
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.phone
import com.tnmaster.repositories.IUserInfoRepo
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 敏感信息模拟接口
 *
 * @author TrueNine
 * @since 2024-12-14
 */
@Api
@RestController
@RequestMapping("v2/sensitiveMock")
class SensitiveMockApi(private val repo: IUserInfoRepo) {
  private fun randomGeneratePhone(): String {
    val therePhoneNumber = (13000000000..13999999999).random().toString()
    val forePhoneNumber = (14700000000..14799999999).random().toString()
    val five1PhoneNumber = (15000000000..15399999999).random().toString()
    val five2PhoneNumber = (15500000000..15999999999).random().toString()
    val sixPhoneNumber = (16300000000..16399999999).random().toString()
    val sevenPhoneNumber = (17000000000..17999999999).random().toString()
    val eightPhoneNumber = (18000000000..18999999999).random().toString()
    val ninePhoneNumber = (1920000000..1929999999).random().toString()
    return listOf(therePhoneNumber, forePhoneNumber, five1PhoneNumber, five2PhoneNumber, sixPhoneNumber, sevenPhoneNumber, eightPhoneNumber, ninePhoneNumber)
      .random()
  }

  /** ## 获取一个当前数据库内不存在的随机电话号码 */
  @Api
  @RequirePermission("ADMIN")
  @GetMapping("global_user_info_unique_phone")
  fun getGlobalNowUserInfoUniquePhone(): String {
    var generatedRandomPhoneNumber = randomGeneratePhone()
    var foundPhone = false
    while (!foundPhone) {
      generatedRandomPhoneNumber = randomGeneratePhone()
      foundPhone =
        repo.sql
          .createQuery(UserInfo::class) {
            where(table.phone eq generatedRandomPhoneNumber)
            where(table.phone.isNotNull())
            orderBy(table.phone.desc())
            select(table.phone)
          }
          .exists()
    }
    return generatedRandomPhoneNumber
  }
}
