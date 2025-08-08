package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.entities.AnonymousCertGroup
import com.tnmaster.repositories.IAnonymousCertGroupRepo
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.Pr
import io.github.truenine.composeserver.rds.toPageable
import io.github.truenine.composeserver.rds.toPr
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** # 第2版审核API */
@Api
@RestController
@RequestMapping("v2/audit")
class AuditV2Api(
  private val repo: IAnonymousCertGroupRepo,
) {

  /**
   * ## 获取所有匿名证件信息
   * > 管理员权限接口，用于获取系统中所有匿名证件信息
   *
   * 该接口支持分页查询，默认返回最大分页结果
   * @param pq 分页查询参数，默认为最大分页配置
   * @return 包含匿名证件组的分页响应结果
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("anonymous_certs")
  fun getAllAnonymousCerts(@RequestBody pq: Pq? = Pq.DEFAULT_MAX): Pr<AnonymousCertGroup> {
    return repo.findAll(pq.toPageable()).toPr()
  }
}
