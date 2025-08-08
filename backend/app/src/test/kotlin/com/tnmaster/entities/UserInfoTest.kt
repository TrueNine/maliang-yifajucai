package com.tnmaster.entities

import com.tnmaster.dto.userinfo.UserInfoAdminSpec
import com.tnmaster.repositories.IBlackListRepo
import com.tnmaster.repositories.IUserInfoRepo
import jakarta.annotation.Resource
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.RelationItemTyping
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@RDBRollback
class UserInfoTest : IDatabasePostgresqlContainer, IOssMinioContainer {
  @Resource
  lateinit var userInfoRepo: IUserInfoRepo

  @Resource
  lateinit var blackListRepo: IBlackListRepo

  @Nested
  inner class IsBlackedVariableGroup {
    @Test
    @ACID
    @RDBRollback
    fun `当 查询参数拥有 isBlacked 时 查询不应卡死`() {
      val savedUserInfo = userInfoRepo.saveCommand(UserInfo {
        idCard = "433126199001014432"
        addressCode = "433126"
        phone = "13711111111"
      }, SaveMode.INSERT_ONLY) {

      }.execute().modifiedEntity
      blackListRepo.saveCommand(BlackList {
        blackUserInfoId = savedUserInfo.id
        eventDoc = "测试拉黑"
        reItemType = RelationItemTyping.CUSTOMER
      }, SaveMode.INSERT_ONLY).execute()
      val foundUserInfo = userInfoRepo.findFirstBySpec(
        UserInfoAdminSpec(
          id = savedUserInfo.id.toString(),
          isBlacked = true
        ),
        fetcher = newFetcher(UserInfo::class).by {
          isBlacked()
        }
      )
      assertEquals(
        savedUserInfo.id,
        foundUserInfo?.id,
        "保存后的用户应当被查询到"
      )
      assertTrue("保存后的用户应当被拉黑处理") {
        foundUserInfo?.isBlacked == true
      }
    }
  }
}
