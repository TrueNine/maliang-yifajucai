package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.RelationItemTyping
import io.github.truenine.composeserver.rds.enums.RelationTyping
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate

@Entity
interface BlackListRelation : IEntity {

  /**
   * 此关系人的类型
   * - 用户
   * - 客户
   * - 企业
   * - ...
   */
  val reItemType: RelationItemTyping?

  /** 关系于此次事件的具体描述 */
  val eventDoc: String?

  /**
   * 与此次事件的类型
   * - 帮凶
   * - 受害者
   * - ...
   */
  val reType: RelationTyping?

  @IdView("blackList")
  val blackListId: RefId?

  @Key
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  val blackList: BlackList?

  @IdView("userInfo")
  val userInfoId: RefId?

  /** 关联的用户信息 */
  @Key
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  val userInfo: UserInfo?

  /** 关联账号 id */
  @IdView("refUserAccount")
  val refId: RefId?

  /** 关联账号 */
  @ManyToOne
  @JoinColumn(name = "ref_id")
  @OnDissociate(DissociateAction.SET_NULL)
  val refUserAccount: UserAccount?
}
