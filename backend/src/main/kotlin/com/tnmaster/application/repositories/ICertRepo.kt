package com.tnmaster.application.repositories

import com.tnmaster.dto.cert.CertAdminSpec
import com.tnmaster.entities.*
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.rds.IRepo
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.enums.CertPointTyping
import io.github.truenine.composeserver.rds.enums.CertTyping
import io.github.truenine.composeserver.rds.fetchPq
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface ICertRepo : IRepo<Cert, RefId> {

  fun findAllByUserAccountIdAndNotAuditCert(
    userAccountId: RefId? = null,
    userInfoId: RefId? = null,
    fetcher: Fetcher<Cert>? = null,
    visible: Boolean? = null,
    page: Pq? = Pq.DEFAULT_MAX,
  ): IPage<Cert> {
    require(userAccountId != null || userInfoId != null) { "userAccountId or userInfoId must not be null" }
    return sql.createQuery(Cert::class) {
      where(
        or(table.userId `eq?` userAccountId, table.userInfoId `eq?` userInfoId),
        or(table.auditStatus eq AuditTyping.NONE, table.auditStatus.isNull()),
        table.visible `eq?` visible,
        table.groupCode.isNotNull(),
      )
      orderBy(table.groupCode, table.doType)
      select(table.fetch(fetcher))
    }.fetchPq(page)
  }

  fun countCertBySpec(spec: CertAdminSpec? = null): Map<RefId, Long> {
    return sql.createQuery(Cert::class) {
      where(spec)
      where(table.userId.isNotNull(), table.groupCode.isNotNull())
      groupBy(table.groupCode, table.doType, table.userId)
      having(count(table.id) ge 1)
      orderBy(count(table.id).desc())
      select(table.userId, count(table.id))
    }.execute().groupBy { it._1!! }.mapValues { (_, v) -> v.sumOf { it._2 } }
  }

  /**
   * ## 删除用户证件，根据 userAccountId 或 userInfoId 删除
   * > 注意，如果传入 id 均为空则不执行
   *
   * @param certType 证件类型
   * @param userAccountId 用户账号ID
   * @param userInfoId 用户信息ID
   */
  fun deleteOrVisibleCertByUserAccountIdOrUserInfoIdAndCertType(
    certType: CertTyping,
    visible: Boolean? = null,
    userAccountIds: List<RefId> = emptyList(),
    userInfoIds: List<RefId> = emptyList(),
  ): Int {
    if (userAccountIds.isEmpty() || userInfoIds.isEmpty()) {
      return 0
    }
    if (visible == false) {
      return sql.createUpdate(Cert::class) {
        set(table.visible, false)
        where(or(table.userId valueIn userAccountIds, table.userInfoId valueIn userInfoIds), table.doType eq certType)
      }.execute()
    }

    return sql.createDelete(Cert::class) { where(or(table.userId valueIn userAccountIds, table.userInfoId valueIn userInfoIds), table.doType eq certType) }
      .execute()
  }

  fun fetchWaterMarkerAttachmentGroupById(ids: List<RefId>, visible: Boolean? = null, fetcher: Fetcher<Cert>? = null): Map<RefId, Cert> {
    return sql.createQuery(Cert::class) {
      where(table.id valueIn ids)
      select(table.fetch(fetcher))
    }.execute().groupBy { it.id }.mapValues { (_, v) -> v.firstOrNull() }.filterValues { it?.waterMarkerAttachment != null }.mapValues { (_, value) -> value!! }
  }

  /**
   * ## 根据用户账号 ID 或用户信息 ID 查询证件
   *
   * @param certType 证件类型
   * @param userAccountIds 用户账号ID
   * @param userInfoIds 用户信息ID
   */
  fun findCertsByUserAccountIdInOrUserInfoIdInAndCertType(
    certType: CertTyping,
    visible: Boolean? = null,
    userAccountIds: List<RefId> = emptyList(),
    userInfoIds: List<RefId> = emptyList(),
    fetcher: Fetcher<Cert>? = null,
  ): List<Cert> {
    require(userAccountIds.isNotEmpty() || userInfoIds.isNotEmpty()) { "userAccountId or userInfoId must not be empty" }
    val vis = visible != false
    if (userAccountIds.isEmpty() && userInfoIds.isEmpty()) {
      return emptyList()
    }
    return sql.createQuery(Cert::class) {
      where(
        if (vis) table.visible `eq?` visible else null,
        or(
          table.userAccount.id `valueIn?` userAccountIds,
          table.userInfoId `valueIn?` userInfoIds,
          table.userAccount.id `valueIn?` userAccountIds,
          table.userInfo.id `valueIn?` userInfoIds,
        ),
        table.doType eq certType,
      )
      select(table.fetch(fetcher))
    }.execute()
  }

  /**
   * ## 根据用户账号 id 或用户信息 id 查询证件
   *
   * @param userAccountIds 用户账号 id
   * @param userInfoIds 用户信息 id
   * @param visible 控制可见度
   * @param fetcher 获取器
   */
  fun findCertsByUserAccountIdInOrUserInfoIdIn(
    userAccountIds: List<RefId?> = emptyList(),
    userInfoIds: List<RefId?> = emptyList(),
    visible: Boolean? = null,
    fetcher: Fetcher<Cert>? = null,
  ): List<Cert> {
    require(userAccountIds.isNotEmpty() || userInfoIds.isNotEmpty()) { "用户账号ID或用户信息ID不能为空" }
    val vis = visible != false
    val uaids = userAccountIds.filterNotNull()
    val uiids = userInfoIds.filterNotNull()
    require(
      uaids.isNotEmpty() || uiids.isNotEmpty()
    ) { "用户账号或用户信息不能为空" }

    return sql.createQuery(Cert::class) {
      where(
        (table.visible `eq?` visible).takeIf { vis },
        or(
          table.userAccount.id `valueIn?` uaids,
          table.userInfoId `valueIn?` uiids,
        ),
      )
      select(table.fetch(fetcher))
    }.execute()
  }

  fun findCertifiedUserInfoBySpec(spec: KSpecification<Cert>, fetcher: Fetcher<UserInfo>? = null): UserInfo? {
    return sql.createQuery(Cert::class) {
      where(spec)
      select(table.userAccount.userInfo.fetch(fetcher))
    }.limit(1).offset(0).execute().firstOrNull()
  }

  /**
   * ## 根据用户账号 id 或用户信息 id 查询证件
   *
   * @param certType 证件类型
   * @param userAccountId 用户账号ID
   * @param userInfoId 用户信息ID
   * @param visible 控制可见度
   * @param fetcher 获取器
   */
  fun findFirstCertByHeadTailDoubleAndUserAccountIdOrUserInfoId(
    certType: CertTyping,
    userAccountId: RefId? = null,
    userInfoId: RefId? = null,
    visible: Boolean? = null,
    fetcher: Fetcher<Cert>? = null,
  ): Pair<Cert?, Cert?> {
    require(userAccountId != null || userInfoId != null) { "userAccountId or userInfoId must not be null" }
    val vis = visible != false

    // 首先尝试查找 DOUBLE 类型的证件
    val doubleCert = sql.createQuery(Cert::class) {
      where += if (vis) table.visible `eq?` visible else null
      where(
        or(table.userId `eq?` userAccountId, table.userInfoId `eq?` userInfoId),
        table.doType eq certType,
        table.poType eq CertPointTyping.DOUBLE
      )
      select(table.fetch(fetcher))
    }.limit(1).offset(0).execute().firstOrNull()

    // 如果找到DOUBLE类型，直接返回
    if (doubleCert != null) {
      return doubleCert to null
    }

    // 否则查找最新的正反面证件
    val headTailCerts = sql.createQuery(Cert::class) {
      where += if (vis) table.visible `eq?` visible else null
      where(
        or(table.userId `eq?` userAccountId, table.userInfoId `eq?` userInfoId),
        table.doType eq certType,
        table.poType valueIn listOf(CertPointTyping.HEADS, CertPointTyping.TAILS)
      )
      orderBy(table.poType.asc(), table.crd.desc())
      select(table.fetch(fetcher))
    }.execute()

    // 分组获取最新的正反面证件
    val groupedCerts = headTailCerts.groupBy { it.poType }
    val heads = groupedCerts[CertPointTyping.HEADS]?.firstOrNull()
    val tails = groupedCerts[CertPointTyping.TAILS]?.firstOrNull()

    return heads to tails
  }
}
