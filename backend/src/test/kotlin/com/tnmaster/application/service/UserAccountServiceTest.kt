package com.tnmaster.application.service

import com.tnmaster.repositories.IBankCardRepo
import com.tnmaster.repositories.ICertRepo
import com.tnmaster.repositories.IUserInfoRepo
import com.tnmaster.dto.cert.CertCreatedInfoDto
import com.tnmaster.dto.userinfo.UserInfoAdminPostDto
import com.tnmaster.dto.userinfo.UserInfoAdminSpec
import com.tnmaster.entities.BankCard
import com.tnmaster.entities.Cert
import com.tnmaster.entities.userAccountId
import com.tnmaster.entities.userId
import com.tnmaster.service.BankService
import com.tnmaster.service.CertService
import com.tnmaster.service.UserAccountService
import com.tnmaster.service.UserAuthService
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
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
class UserAccountServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

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
    lateinit var userAuthService: UserAuthService

    @Test
    @RDBRollback
    fun normal_create_system_account_should_return_complete_user_info() {
      // Given
      val userInfo = UserInfoAdminPostDto(
        firstName = "赵",
        lastName = "日天"
      )

      // When
      val result = userAuthService.assignSystemAccount(baseUserInfo = userInfo)

      // Then
      assertEquals("赵", result.firstName)
      assertEquals("日天", result.lastName)
      assertNotNull(result.userAccountId, "用户账户ID不应为空")
    }
  }

  @Nested
  inner class PostBankCardAttachmentFunctionGroup {

    @Resource
    lateinit var userAuthService: UserAuthService

    @Resource
    lateinit var certRepo: ICertRepo

    @org.junit.jupiter.api.Disabled("需要MinIO bucket 'meta-certs'")
    @Test
    @RDBRollback
    fun normal_add_bank_card_attachment_should_create_bank_card_and_cert_records() {
      // Given - 先创建用户账户
      val userInfo = UserInfoAdminPostDto(
        firstName = "张",
        lastName = "三"
      )
      val createdUser = userAuthService.assignSystemAccount(baseUserInfo = userInfo)
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
    lateinit var userAuthService: UserAuthService

    @Resource
    lateinit var userInfoRepo: IUserInfoRepo

    @Resource
    lateinit var bankRepo: IBankCardRepo

    @org.junit.jupiter.api.Disabled("需要MinIO bucket 'meta-certs'")
    @Test
    @RDBRollback
    fun normal_query_user_info_by_bank_name_should_return_matching_user_list() {
      // Given - 创建用户并添加银行卡
      val userInfo = UserInfoAdminPostDto(
        firstName = "赵",
        lastName = "日天"
      )
      val createdUser = userAuthService.assignSystemAccount(baseUserInfo = userInfo)
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
