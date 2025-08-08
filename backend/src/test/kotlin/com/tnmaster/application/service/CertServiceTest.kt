package com.tnmaster.application.service

import com.tnmaster.dto.cert.CertAdminPostDto
import com.tnmaster.dto.cert.CertCreatedInfoDto
import com.tnmaster.entities.Attachment
import com.tnmaster.entities.Cert
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
import com.tnmaster.application.repositories.ICertRepo
import com.tnmaster.application.repositories.IUserAccountRepo
import com.tnmaster.application.repositories.IUserInfoRepo
import jakarta.annotation.Resource
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.*
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.log
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import io.github.truenine.composeserver.toId
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Transactional
@RDBRollback
class CertServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {
  @Resource
  private lateinit var userInfoRepo: IUserInfoRepo

  @Resource
  private lateinit var certRepo: ICertRepo

  @Resource
  private lateinit var certService: CertService

  @Resource
  private lateinit var userAccountRepo: IUserAccountRepo

  @Nested
  inner class PostCertWaterMarkerImageAttachmentsFunctionGroup {
    @Test
    fun `正常 上传单个有效证件附件时，应成功保存并返回正确结果`() {
      // 先插入 user_account
      val userAccountId = 9999L
      userAccountRepo.saveCommand(UserAccount {
        account = "test_account_1"
        pwdEnc = "test_pwd"
      })
      // 再插入 user_info，userAccountId 关联，id 由 ORM 自动生成
      val userInfo = userInfoRepo.saveCommand(UserInfo {
        this.userAccountId = userAccountId
        name = "测试用户"
        phone = "13800138000"
      }) {
        setKeyProps(UserInfo::phone)
      }.execute().modifiedEntity
      val userInfoId = userInfo.id
      val certDto = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, userAccountId.toString(), userInfoId.toString() // userInfoId
      )
      val testFile = MockMultipartFile(
        "test.jpg",
        "test.jpg",
        "image/jpeg",
        CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(1024) { 0xFF.toByte() })
      val createInfo = CertCreatedInfoDto(
        "127.0.0.1", // createIp
        "test-device", // createDeviceId
        datetime.now(), // createDatetime
        0L.toString(), // createUserId
        null // groupCode
      )

      // 执行测试
      val result = certService.postCertWaterMarkerImageAttachments(
        certDtos = listOf(certDto), files = listOf(testFile), userAccountId = userAccountId, createInfo = createInfo
      )

      // 验证结果
      assertNotNull(result)
      assertEquals(1, result.size)

      // 验证数据库中的记录
      val savedCert = certRepo.findById(result[0].id).get()
      assertEquals(userAccountId, savedCert.userId)
      assertEquals(AuditTyping.NONE, savedCert.auditStatus)
    }

    @Test
    fun `正常 上传多个有效证件附件时，应全部成功保存并返回正确结果`() {
      // 先插入 user_account
      val userAccountId = 9999L
      userAccountRepo.saveCommand(UserAccount {
        account = "test_account_1"
        pwdEnc = "test_pwd"
      })
      // 再插入 user_info，userAccountId 关联，id 由 ORM 自动生成
      val userInfo = userInfoRepo.saveCommand(UserInfo {
        this.userAccountId = userAccountId
        name = "测试用户"
        phone = "13800138000"
      }) {
        setKeyProps(UserInfo::phone)
      }.execute().modifiedEntity
      val userInfoId = userInfo.id
      val certDtos = listOf(
        CertAdminPostDto(
          CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, userAccountId.toString(), userInfoId.toString() // userInfoId
        ), CertAdminPostDto(
          CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.TAILS, userAccountId.toString(), userInfoId.toString() // userInfoId
        )
      )
      val files = listOf(
        MockMultipartFile(
          "front.jpg",
          "front.jpg",
          "image/jpeg",
          CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(1024) { 0xFF.toByte() }), MockMultipartFile(
          "back.jpg",
          "back.jpg",
          "image/jpeg",
          CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(1024) { 0xFF.toByte() })
      )
      val createInfo = CertCreatedInfoDto(
        "127.0.0.1", // createIp
        "test-device", // createDeviceId
        datetime.now(), // createDatetime
        0L.toString(), // createUserId
        null // groupCode
      )

      // 执行测试
      val result = certService.postCertWaterMarkerImageAttachments(
        certDtos = certDtos, files = files, userAccountId = userAccountId, createInfo = createInfo
      )

      // 验证结果
      assertNotNull(result)
      assertEquals(2, result.size)

      // 验证数据库中的记录
      result.forEach { cert ->
        val savedCert = certRepo.findById(cert.id).get()
        assertEquals(userAccountId, savedCert.userId)
        assertEquals(AuditTyping.NONE, savedCert.auditStatus)
      }
    }

    // --- Start of merged exception tests ---
    @Test
    fun `边界 输入空列表时，应返回空结果`() {
      val result = certService.postCertWaterMarkerImageAttachments(
        certDtos = emptyList(), files = emptyList(), userAccountId = 1L, createInfo = CertCreatedInfoDto(
          createUserId = 0L.toString()
        )
      )

      assertEquals(0, result.size)
    }

    @Test
    fun `异常 证件数量与文件数量不匹配时，抛出 IllegalArgumentException`() {
      val certDto1 = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, "1", // userAccountId
        null // userInfoId
      )
      val file1 = MockMultipartFile(
        "test.jpg", "test.jpg", "image/jpeg", CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(0)
      )

      val exception1 = assertThrows<IllegalArgumentException> {
        certService.postCertWaterMarkerImageAttachments(
          certDtos = listOf(certDto1, certDto1), // 两个证件DTO
          files = listOf(file1), // 但只有一个文件
          userAccountId = 1L, createInfo = CertCreatedInfoDto(
            createUserId = 0L.toString()
          )
        )
      }

      assertEquals("certs.size != files.size", exception1.message)
    }

    @Test
    fun `异常 证件中用户信息为空时，抛出 IllegalArgumentException`() {
      val certDto2 = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, null, // userAccountId
        null // userInfoId
      )
      val file2 = MockMultipartFile(
        "test.jpg", "test.jpg", "image/jpeg", CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(0)
      )

      val exception2 = assertThrows<IllegalArgumentException> {
        certService.postCertWaterMarkerImageAttachments(
          certDtos = listOf(certDto2), files = listOf(file2), userAccountId = 1L, createInfo = CertCreatedInfoDto(
            createUserId = 0L.toString()
          )
        )
      }

      assertEquals("证件组中存在用户信息为空的证件", exception2.message)
    }

    @Test
    fun `异常 上传空文件时，抛出 IllegalArgumentException`() {
      val userAccountId3 = 1L
      val certDto3 = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, userAccountId3.toString(), null // userInfoId
      )

      // 创建一个空的MultipartFile
      val emptyFile = MockMultipartFile(
        "empty.jpg", // name
        "empty.jpg", // originalFilename
        "image/jpeg", // contentType
        ByteArray(0)  // content
      )

      val createInfo3 = CertCreatedInfoDto(
        "127.0.0.1",
        "test-device",
        datetime.now(),
        0L.toString(),
        null
      )

      val exception3 = assertThrows<IllegalArgumentException> {
        certService.postCertWaterMarkerImageAttachments(
          certDtos = listOf(certDto3), files = listOf(emptyFile), userAccountId = userAccountId3, createInfo = createInfo3
        )
      }

      assertEquals("无法读取图片文件，可能是不支持的图片格式或文件已损坏", exception3.message)
    }

    @Test
    fun `异常 上传无效文件类型时，抛出 IllegalArgumentException`() {
      val userAccountId4 = 1L
      val certDto4 = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, userAccountId4.toString(), null // userInfoId
      )

      // 创建一个contentType为null的MultipartFile
      val fileWithNullContentType = MockMultipartFile(
        "test.jpg", // name
        "test.jpg", // originalFilename
        null,  // contentType - 使用允许为 null 的构造函数
        CertServiceTest::class.java.getResourceAsStream("/test-images/sample.jpg")?.readBytes() ?: ByteArray(0) // content
      )

      val createInfo4 = CertCreatedInfoDto(
        "127.0.0.1", // createIp
        "test-device", // createDeviceId
        datetime.now(), // createDatetime
        0L.toString(), // createUserId
        null // groupCode
      )

      // 执行测试并验证异常
      assertThrows<IllegalArgumentException> {
        certService.postCertWaterMarkerImageAttachments(
          certDtos = listOf(certDto4), files = listOf(fileWithNullContentType), userAccountId = userAccountId4, createInfo = createInfo4
        )
      }
    }

    @Test
    fun `异常 上传无法读取的图片文件时，抛出 IllegalArgumentException`() {
      val userAccountId5 = 9999L // 使用一个不会查出 userInfoId 的账号ID，避免一致性校验提前触发
      val certDto5 = CertAdminPostDto(
        CertTyping.ID_CARD2, CertContentTyping.SCANNED_IMAGE, CertPointTyping.HEADS, userAccountId5.toString(), null // userInfoId
      )

      val corruptedImageFile = MockMultipartFile(
        "corrupted.jpg",
        "corrupted.jpg",
        "image/jpeg",
        "这不是有效的JPEG数据".toByteArray()
      )

      val createInfo5 = CertCreatedInfoDto(
        "127.0.0.1",
        "test-device", // createDeviceId
        datetime.now(), // createDatetime
        0L.toString(), // createUserId
        null // groupCode
      )
      val exception5 = assertThrows<IllegalArgumentException> {
        certService.postCertWaterMarkerImageAttachments(
          certDtos = listOf(certDto5), files = listOf(corruptedImageFile), userAccountId = userAccountId5, createInfo = createInfo5
        )
      }

      assertEquals("无法读取图片文件，可能是不支持的图片格式或文件已损坏", exception5.message)
    }
  }

  @Nested
  inner class FixAllImagedCertTypingMarkForUserInfoOrAccountFunctionGroup {

    @Test
    @ACID
    @RDBRollback
    fun `正常 仅 savedUserInfoId 有效时，应修正所有 certs 的 coType doType poType 字段`() {
      // 先插入 user_account
      val savedUserInfoId = 9999L
      userAccountRepo.saveCommand(UserAccount {
        id = savedUserInfoId
        account = "test_account_1"
        pwdEnc = "test_pwd"
      }).execute()
      // 插入用户信息
      userInfoRepo.saveCommand(UserInfo {
        this.userAccountId = savedUserInfoId
        firstName = "张"
        lastName = "三"
        phone = "13800138000"
      }).execute()
      certRepo.saveCommand(Cert {
        userInfoId = savedUserInfoId
        userId = savedUserInfoId
        coType = CertContentTyping.IMAGE
        doType = CertTyping.ID_CARD
        poType = CertPointTyping.ALL
        wmCode = "2"
        groupCode = "2"
        createUserId = 0.toId()!!
        waterMarkerAttachment = Attachment {
          attType = AttachmentTyping.ATTACHMENT
          saveName = "0011"
          metaName = "a.jpg"
        }
        metaAttachment = Attachment {
          attType = AttachmentTyping.ATTACHMENT
          saveName = "0012"
          metaName = "b.jpg"
        }
      }) {
        setKeyProps(
          "file", Attachment::attType, Attachment::saveName, Attachment::metaName
        )
      }.execute()
      certRepo.saveCommand(Cert {
        userInfoId = savedUserInfoId
        userId = savedUserInfoId
        coType = CertContentTyping.SCANNED_IMAGE
        doType = CertTyping.BANK_CARD
        poType = CertPointTyping.HEADS
        wmCode = "1"
        groupCode = "1"
        createUserId = 0.toId()!!
        waterMarkerAttachment = Attachment {
          attType = AttachmentTyping.ATTACHMENT
          saveName = "0011"
          metaName = "a.jpg"
        }
        metaAttachment = Attachment {
          attType = AttachmentTyping.ATTACHMENT
          saveName = "0012"
          metaName = "b.jpg"
        }
      }) {
        setKeyProps(
          "file", Attachment::attType, Attachment::saveName, Attachment::metaName
        )
      }.execute()

      val result = certService.fixAllImagedCertTypingMarkForUserInfoOrAccount(userInfoId = savedUserInfoId)

      assertEquals(2, result.size)
      assertEquals(CertContentTyping.PROCESSED_SCANNED_IMAGE, result[0].coType)
      assertEquals(CertTyping.ID_CARD2, result[0].doType)
      assertEquals(CertPointTyping.DOUBLE, result[0].poType)
      assertEquals(CertContentTyping.SCANNED_IMAGE, result[1].coType)
      assertEquals(CertTyping.BANK_CARD, result[1].doType)
      assertEquals(CertPointTyping.HEADS, result[1].poType)
    }

    @Test
    @ACID
    @RDBRollback
    fun `正常 仅 userInfoId 有效时，应修正所有 certs 的 coType doType poType 字段`() {
      // 先插入 user_account
      val userAccountId = 8888L
      userAccountRepo.saveCommand(UserAccount {
        id = userAccountId
        account = "test_account_2"
        pwdEnc = "test_pwd"
      }).execute()
      // 插入用户信息
      val userInfo = userInfoRepo.saveCommand(UserInfo {
        this.userAccountId = userAccountId
        firstName = "李"
        lastName = "四"
        phone = "13900139000"
      }) {
        setKeyProps(UserInfo::phone)
      }.execute().modifiedEntity
      log.info("saved user info: {}", userInfo)
      val userInfoId = userInfo.id
      // 插入证件
      certRepo.saveCommand(
        Cert {
          this.userInfoId = userInfoId
          this.userId = userAccountId
          coType = CertContentTyping.COPYFILE_IMAGE
          doType = CertTyping.DISABILITY_CARD
          poType = CertPointTyping.ALL_CONTENT
          wmCode = "2"
          groupCode = "2"
          createUserId = 0.toId()!!
          waterMarkerAttachment = Attachment {
            attType = AttachmentTyping.ATTACHMENT
            saveName = "0011"
            metaName = "a.jpg"
          }
          metaAttachment = Attachment {
            attType = AttachmentTyping.ATTACHMENT
            saveName = "0012"
            metaName = "b.jpg"
          }
        }, SaveMode.INSERT_ONLY
      ) {
        setKeyProps(
          "file", Attachment::attType, Attachment::saveName, Attachment::metaName
        )
      }.execute()

      val result = certService.fixAllImagedCertTypingMarkForUserInfoOrAccount(userInfoId = userInfoId)

      assertEquals(1, result.size)
      assertEquals(CertContentTyping.PROCESSED_SCANNED_IMAGE, result[0].coType)
      assertEquals(CertTyping.DISABILITY_CARD3, result[0].doType)
      assertEquals(CertPointTyping.DOUBLE, result[0].poType)
    }

    @Test
    fun `异常 userAccountId 和 userInfoId 同时为 null 时，抛出 IllegalArgumentException`() {
      val exception = assertThrows<IllegalArgumentException> {
        certService.fixAllImagedCertTypingMarkForUserInfoOrAccount()
      }
      assertEquals("用户或用户信息至少需要其中一个", exception.message)
    }

    @Test
    fun `边界 certRepo 返回空列表时，应返回空`() {
      // 先插入 user_account
      val userAccountId = 300L
      userAccountRepo.saveCommand(UserAccount {
        id = userAccountId
        account = "test_account_3"
        pwdEnc = "test_pwd"
      })
      // 插入一个用户账号，但不插入证件
      userInfoRepo.saveCommand(UserInfo {
        this.userAccountId = userAccountId
        firstName = "王"
        lastName = "五"
        phone = "13700137000"
      })
      // 调用 service
      val result = certService.fixAllImagedCertTypingMarkForUserInfoOrAccount(userAccountId = userAccountId)
      // 断言
      assertEquals(0, result.size)
    }
  }
}
