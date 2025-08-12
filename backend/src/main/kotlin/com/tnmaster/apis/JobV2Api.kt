package com.tnmaster.apis

import com.tnmaster.security.annotations.RequirePermission
import com.tnmaster.dto.job.JobAdminSpec
import com.tnmaster.entities.Job
import com.tnmaster.entities.by
import com.tnmaster.entities.orderedWeight
import com.tnmaster.repositories.IJobRepo
import io.github.truenine.composeserver.Pr
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.fetchPq
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

/** 第二版职位 api */
@Api
@RestController
@RequestMapping("v2/job")
class JobV2Api(private val repo: IJobRepo) {
  /**
   * ## 管理员改变职位审核状态（管理员接口）
   * TODO 管理员接口
   *
   * @param id 职位 id
   * @param auditStatus 审核状态
   */
  @Api
  @RequirePermission("admin:manage")
  @PatchMapping("admin/auditStatus/{id}")
  fun patchAuditStatusById(@PathVariable id: RefId, @RequestParam auditStatus: AuditTyping): Job? {
    if (!repo.existsById(id)) {
      return null
    }
    return repo.update(
      Job {
        this.id = id
        this.auditStatus = auditStatus
      }
    )
  }

  /**
   * ## 根据条件查询职位（管理员接口）
   *
   * @param spec 查询条件和分页参数
   */
  @Api
  @GetMapping("admin")
  fun getAdminJobBySpec(@RequestBody spec: JobAdminSpec? = null): Pr<Job> {
    return repo.sql
      .createQuery(Job::class) {
        where(spec)
        select(table.fetch(fetcher = ADMIN_JOB))
      }
      .fetchPq(spec)
  }

  /**
   * ## 根据 id 查询职位（管理员接口）
   *
   * @param id jobId
   * @return 管理员可查询到的职位
   */
  @Api
  @GetMapping("admin/id/{id}")
  fun getAdminJobById(@PathVariable id: RefId): @FetchBy("ADMIN_JOB") Job? {
    return repo.findById(id, ADMIN_JOB).getOrNull()
  }

  /** 客户查询所有职位 */
  @Api
  @GetMapping("customer")
  fun findAllFromCustomer(): List<Job> {
    return repo.sql
      .createQuery(Job::class) {
        orderBy(table.orderedWeight)
        select(table.fetch(CUSTOMER_JOB))
      }
      .execute()
  }

  companion object {
    val ADMIN_JOB = newFetcher(Job::class).by { allScalarFields() }
    val CUSTOMER_JOB =
      newFetcher(Job::class).by {
        title()
        doc()
        phone()
        lifecycle()
        userInfo { disInfo() }
      }
  }
}
