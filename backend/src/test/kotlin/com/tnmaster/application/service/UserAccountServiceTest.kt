package com.tnmaster.application.service

import com.tnmaster.dto.cert.CertCreatedInfoDto
import com.tnmaster.dto.userinfo.UserInfoAdminPostDto
import com.tnmaster.dto.userinfo.UserInfoAdminSpec
import com.tnmaster.entities.BankCard
import com.tnmaster.entities.Cert
import com.tnmaster.entities.userAccountId
import com.tnmaster.entities.userId
import com.tnmaster.application.repositories.IBankCardRepo
import com.tnmaster.application.repositories.ICertRepo
import com.tnmaster.application.repositories.IUserInfoRepo
import jakarta.annotation.Resource
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class UserAccountServiceTest : IDatabasePostgresqlContainer {

  @Resource
  lateinit var userAccountService: UserAccountService

  @Resource
  lateinit var certService: CertService

  @Resource
  lateinit var bankService: BankService

  @BeforeTest
  fun setup() {
    val banks = bankService.fetchAllBanks()
    assertTrue("银行列表不应为空") { banks.isNotEmpty() }
    assertTrue("银行列表应包含中国银行") { banks.map { it.bankName }.contains("中国银行") }
  }

  /**
   * 创建模拟的 MultipartFile，从测试资源中读取图片
   */
  private fun createMockMultipartFile(fileName: String = "kotlin_1.png"): MultipartFile {
    val inputStream = this::class.java.classLoader.getResourceAsStream("test-images/$fileName")
      ?: error("测试图片文件不存在: $fileName")

    val bytes = inputStream.use { it.readBytes() }

    return MockMultipartFile(
      "file",
      fileName,
      "image/png",
      bytes
    )
  }

  @Nested
  inner class AssignSystemAccountFunctionGroup {

    @Resource
    lateinit var authService: UserAuthService

    @Test
    @RDBRollback
    fun `正常 创建系统账户 时，应返回完整用户信息`() {
      // Given
      val userInfo = UserInfoAdminPostDto(
        firstName = "赵",
        lastName = "日天"
      )

      // When
      val result = authService.assignSystemAccount(baseUserInfo = userInfo)

      // Then
      assertEquals("赵", result.firstName)
      assertEquals("日天", result.lastName)
      assertNotNull(result.userAccountId, "用户账户ID不应为空")
    }
  }

  @Nested
  inner class PostBankCardAttachmentFunctionGroup {

    @Resource
    lateinit var authService: UserAuthService

    @Resource
    lateinit var certRepo: ICertRepo

    @Test
    @RDBRollback
    fun `正常 添加银行卡附件 时，应创建银行卡和证件记录`() {
      // Given - 先创建用户账户
      val userInfo = UserInfoAdminPostDto(
        firstName = "张",
        lastName = "三"
      )
      val createdUser = authService.assignSystemAccount(baseUserInfo = userInfo)
      val userAccountId = createdUser.userAccountId!!

      val bankCard = BankCard {
        code = UUID.randomUUID().toString()
        bankName = "中国银行"
        bankGroupName = "UNI_PAY"
        phone = "12345678901"
        this.userAccountId = userAccountId
      }
      val mockImageFile = createMockMultipartFile("kotlin_1.png")

      // When
      val savedBankCard = certService.postBankCardAttachment(
        userAccountId = userAccountId,
        baseBankCard = BankCard {
          code = UUID.randomUUID().toString()
          bankName = "中国银行"
          bankGroupName = "UNI_PAY"
          phone = "12345678901"
          this.userAccountId = userAccountId
        },
        head = mockImageFile,
        createInfo = CertCreatedInfoDto()
      )

      // Then
      assertEquals(userAccountId, savedBankCard.userAccountId)
      assertEquals(1, savedBankCard.certs.size)

      val firstCert = savedBankCard.certs.first()
      assertEquals(userAccountId, firstCert.userId)
      assertEquals(createdUser.id, firstCert.userInfoId)

      // 验证证件记录已保存到数据库
      val savedCerts = certRepo.sql.createQuery(Cert::class) {
        where(table.userId eq userAccountId)
        select(table)
      }.execute()

      assertTrue("应存在证件记录") { savedCerts.isNotEmpty() }
    }
  }

  @Nested
  inner class FetchUserInfosAsAdminFunctionGroup {

    @Resource
    lateinit var authService: UserAuthService

    @Resource
    lateinit var userInfoRepo: IUserInfoRepo

    @Resource
    lateinit var bankRepo: IBankCardRepo

    @Test
    @RDBRollback
    fun `正常 按银行名称查询用户信息 时，应返回匹配的用户列表`() {
      // Given - 创建用户并添加银行卡
      val userInfo = UserInfoAdminPostDto(
        firstName = "赵",
        lastName = "日天"
      )
      val createdUser = authService.assignSystemAccount(baseUserInfo = userInfo)
      val userAccountId = createdUser.userAccountId!!

      // 添加银行卡附件
      val mockImageFile = createMockMultipartFile("kotlin_1.png")
      certService.postBankCardAttachment(
        userAccountId = userAccountId,
        baseBankCard = BankCard {
          code = UUID.randomUUID().toString()
          bankName = "中国银行"
          bankGroupName = "UNI_PAY"
          phone = "12345678901"
          this.userAccountId = userAccountId
        },
        head = mockImageFile,
        createInfo = CertCreatedInfoDto()
      )

      // 验证银行卡已保存
      val savedBankCards = bankRepo.sql.createQuery(BankCard::class) {
        where(table.userAccountId eq userAccountId)
        select(table)
      }.execute()
      assertTrue("银行卡记录应已保存") { savedBankCards.isNotEmpty() }

      // When - 按条件查询用户信息
      val spec = UserInfoAdminSpec(
        name = "赵日天",
        certsBankGroupValueIn = listOf("中国银行"),
      )
      val results = userAccountService.fetchUserInfosAsAdmin(spec)

      // Then
      assertTrue("查询结果不应为空") { results.d.isNotEmpty() }
    }
  }
}
