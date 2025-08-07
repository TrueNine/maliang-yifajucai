package com.tnmaster.service

import com.tnmaster.dto.blacklist.BlackListAdminPostDto
import com.tnmaster.entities.BlackList
import com.tnmaster.repositories.IBlackListRepo
import com.tnmaster.repositories.IUserInfoRepo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.annotations.ACID
import org.springframework.stereotype.Service

@Service
class BlackListService(
  private val blackListRepo: IBlackListRepo,
  private val userInfoRepo: IUserInfoRepo,
) {

  /**
   * ## 创建黑名单记录
   * 使用事务保证数据一致性，自动关联用户信息并创建黑名单实体
   *
   * @param createUserAccountId 操作人用户账户ID（需关联用户信息表）
   * @param dto 黑名单数据传输对象，包含前端提交的字段数据
   * @return 持久化后的黑名单实体对象，包含数据库生成的字段
   *
   * ### 实现说明
   * 1. 通过`userInfoRepo`根据账户ID获取关联的用户信息ID
   * 2. 将`createUserAccountId`同时作为操作人和上报人ID
   * 3. 使用Jimmer的`toEntity`转换DTO时自动填充审计字段
   */
  @ACID
  fun postBlackList(
    createUserAccountId: RefId,
    dto: BlackListAdminPostDto,
  ): BlackList {
    return blackListRepo.insert(dto.toEntity {
      reportUserInfoId = userInfoRepo.findFirstIdByUserAccountId(createUserAccountId)
      createUserId = createUserAccountId
      reportUserId = createUserAccountId
    })
  }
}
