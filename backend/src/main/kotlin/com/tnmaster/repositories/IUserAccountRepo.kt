package com.tnmaster.repositories

import com.tnmaster.entities.RoleGroup
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.account
import com.tnmaster.entities.by
import com.tnmaster.entities.fetchBy
import com.tnmaster.entities.id
import com.tnmaster.entities.lastLoginTime
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.IRepo
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IUserAccountRepo : IRepo<UserAccount, RefId> {
  /** ## 更新某账号的最后登录时间 */
  fun updateLastLoginTimeToNowByAccount(account: String) {
    sql
      .createUpdate(UserAccount::class) {
        where(table.account eq account)
        set(table.lastLoginTime, datetime.now())
      }
      .execute()
  }

  fun findUserAccountByAccount(account: String, fetcher: Fetcher<UserAccount>? = null): UserAccount? {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }

  fun findIdByAccount(account: String): RefId? {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)
        select(table.id)
      }
      .fetchOneOrNull()
  }

  fun findDisabledInfoByAccount(account: String): UserAccount? {
    return sql
      .createQuery(UserAccount::class) {
        where += table.account eq account
        select(
          table.fetchBy {
            account()
            lastLoginTime()
            banTime()
          }
        )
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }

  fun findPwdEncByAccount(account: String): String? {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)
        select(table.fetchBy { metadataPwdEnc() })
      }
      .fetchOneOrNull()
      ?.metadataPwdEnc
  }

  fun findAccountByIdOrNull(id: RefId): String? {
    return findById(id, fetcher = newFetcher(UserAccount::class).by { account() }).orElse(null)?.account
  }

  fun findAllPermissionsNameByAccount(account: String): Set<String> {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)
        select(table.fetchBy { roleGroups { roles { permissions { name() } } } })
      }
      .execute()
      .flatMap { it.roleGroups }
      .flatMap { it.roles }
      .flatMap { it.permissions }
      .map { it.name }
      .toSet()
  }

  fun findAllRoleGroupNameByAccount(account: String): Set<String> {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)
        select(table.fetchBy { roleGroups { name() } })
      }
      .execute()
      .map { it.roleGroups }
      .flatMap { it.map { roleGroup -> roleGroup.name } }
      .toSet()
  }

  fun findAllRoleNameByAccount(account: String): Set<String> {
    return sql
      .createQuery(UserAccount::class) {
        where(table.account eq account)

        select(table.fetchBy { roleGroups { roles { name() } } })
      }
      .execute()
      .asSequence()
      .map { it.roleGroups }
      .flatten()
      .flatMap { it.roles }
      .map { it.name }
      .toSet()
  }

  fun findRoleGroupsByAccount(account: String?, fetcher: Fetcher<RoleGroup> = newFetcher(RoleGroup::class).by { allScalarFields() }): List<RoleGroup> {
    return sql
      .createQuery(UserAccount::class) {
        where += table.account eq account

        select(table.fetchBy { roleGroups(fetcher) })
      }
      .execute()
      .map { it.roleGroups }
      .flatten()
  }

  /**
   * 查询所有用户-角色组关系，用于 Casbin 策略加载
   */
  fun findAllUserRoleGroups(): List<Pair<String, String>> {
    return sql
      .createQuery(UserAccount::class) {
        select(table.fetchBy { 
          account()
          roleGroups { name() } 
        })
      }
      .execute()
      .flatMap { userAccount ->
        userAccount.roleGroups.map { roleGroup ->
          userAccount.account to roleGroup.name
        }
      }
  }

  /**
   * 更新用户最后登录时间
   */
  fun updateLastLoginTimeByAccount(account: String, loginTime: datetime) {
    sql
      .createUpdate(UserAccount::class) {
        where(table.account eq account)
        set(table.lastLoginTime, loginTime)
      }
      .execute()
  }
}
