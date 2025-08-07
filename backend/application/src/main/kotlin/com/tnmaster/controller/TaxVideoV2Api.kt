package com.tnmaster.controller

import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.entities.JobSeekerDisNominalTaxVideo
import com.tnmaster.repositories.IJobSeekerDisNominalTaxVideoRepo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.rds.typing.AuditTyping
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.*

/** # 第2版个税视频接口 */
@Api
@RestController
@RequestMapping("v2/taxVideo")
class TaxVideoV2Api(private val repo: IJobSeekerDisNominalTaxVideoRepo) {
  /**
   * ## 根据 id 改变 审核状态
   * TODO 管理员、审核员接口
   *
   * @param id 个税视频 id
   * @param auditStatus 审核状态
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PatchMapping("admin/auditStatus/{id}")
  fun patchAuditStatusById(@PathVariable id: RefId, @RequestParam auditStatus: AuditTyping, @ApiIgnore auth: AuthRequestInfo): JobSeekerDisNominalTaxVideo {
    return repo.update(
      JobSeekerDisNominalTaxVideo {
        this.id = id
        this.auditStatus = auditStatus
        auditUserId = auth.userId
      }
    )
  }
}
