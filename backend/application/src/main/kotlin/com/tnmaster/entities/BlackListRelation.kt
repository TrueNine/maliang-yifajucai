package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.RelationItemTyping
import io.github.truenine.composeserver.rds.typing.RelationTyping
import org.babyfish.jimmer.sql.*

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
