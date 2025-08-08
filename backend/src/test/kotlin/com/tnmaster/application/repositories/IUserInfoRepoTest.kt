package com.tnmaster.application.repositories

import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.by
import com.tnmaster.entities.name
import jakarta.annotation.Resource
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.babyfish.jimmer.sql.ast.LikeMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.like
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RDBRollback
@SpringBootTest
class IUserInfoRepoTest : IDatabasePostgresqlContainer {
  @Resource
  lateinit var userInfoRepo: IUserInfoRepo

  @Nested
  @RDBRollback
  inner class UserInfoVariableGroup {
    @Test
    @ACID
    fun `确保 name concat 后查询正确`() {
      val nameFetcher = newFetcher(UserInfo::class).by {
        firstName()
        lastName()
        name()
      }
      val savedUserInfo = userInfoRepo.saveCommand(
        UserInfo {
          firstName = "赵"
          lastName = "日天"
        }, SaveMode.INSERT_ONLY
      ).execute(nameFetcher).modifiedEntity
      val firstName = savedUserInfo.firstName
      val lastName = savedUserInfo.lastName
      val name = savedUserInfo.name
      assertEquals(firstName + lastName, name)

      val likeQueryResult = userInfoRepo.sql.createQuery(UserInfo::class) {
        where(
          table.name.like("赵日天", LikeMode.START)
        )
        select(table.fetch(nameFetcher))
      }.execute()
      assertTrue { likeQueryResult.isNotEmpty() }
      assertEquals(
        "赵日天",
        likeQueryResult.first().name
      )
    }
  }

  @Nested
  inner class FindFirstIdByUserAccountIdFunctionGroup {
    private lateinit var userInfo: UserInfo

    @ACID
    fun generatedUserInfo(): UserInfo {
      return userInfoRepo.save(UserInfo {
        firstName = "赵"
        lastName = "日天"
        account = UserAccount {
          account = "zhao_ritian"
          pwdEnc = "dqweqweqwe"
        }
      })
    }

    @Test
    @ACID
    @RDBRollback
    fun `确保 查询 用户信息id 与 用户账号id 一致`() {
      userInfo = generatedUserInfo()

      assertNotNull(userInfo.userAccountId)
      assertNotNull(userInfo.id)

      val allResults = userInfoRepo.findAll()
      assertTrue {
        allResults.isNotEmpty()
      }

      assertTrue {
        allResults.any {
          it.id == userInfo.id && it.userAccountId == userInfo.userAccountId
        }
      }

      val userInfoId = userInfoRepo.findFirstIdByUserAccountId(
        userInfo.userAccountId!!
      )

      assertEquals(
        userInfo.id, userInfoId
      )
    }
  }
}
