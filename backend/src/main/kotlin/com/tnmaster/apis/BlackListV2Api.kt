package com.tnmaster.apis

import com.tnmaster.security.annotations.RequirePermission
import com.tnmaster.dto.blacklist.BlackListAdminPostDto
import com.tnmaster.dto.blacklist.BlackListAdminView
import com.tnmaster.dto.blacklist.BlackListEventDocSpec
import com.tnmaster.dto.blacklist.BlackListEventDocView
import com.tnmaster.entities.*
import com.tnmaster.repositories.IBlackListRepo
import com.tnmaster.service.BlackListService
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.Pr
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.jimmerextpostgres.substr
import io.github.truenine.composeserver.rds.toFetcher
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.springframework.web.bind.annotation.*
import kotlin.math.absoluteValue

/**
 * # 黑名单管理 API V2
 * > 提供黑名单的增删改查功能
 *
 * 该控制器负责处理与黑名单相关的所有请求，包括添加、删除、查询等操作。
 * 使用 V2 版本的 API 路径，确保与旧版本兼容。
 *
 * @param blackListRepo 黑名单数据访问层接口，用于与数据库交互
 */
@Api
@RestController
@RequestMapping("v2/black_list")
class BlackListV2Api(
  private val blackListRepo: IBlackListRepo,
  private val blackListService: BlackListService,
) {

  /**
   * ## 查询多个省份的统计信息
   *
   * @param rankNum 统计数量 默认为 10
   * @return 省份代码，统计数量
   */
  @Api
  @GetMapping("statistics/provinces_rank")
  fun getProvincesStatistics(@RequestParam rankNum: Int = 10): Map<String, Long> {
    val r = if (rankNum > 10) 10 else rankNum.absoluteValue
    return blackListRepo.sql.createQuery(BlackList::class) {
      val a = table.blackUserInfo.addressCode.substr(1, 2)
      val b = count(table.id)
      groupBy(a)
      where(table.blackUserInfo.addressCode.isNotNull())
      orderBy(b.desc())
      select(a, b)
    }.limit(r).offset(0).execute().associate { (a, b) -> a to b }
  }

  /**
   * ## 管理员添加黑名单
   * > 该接口仅限管理员权限使用，用于将用户加入黑名单
   *
   * 该接口接收管理员提交的黑名单信息，并自动填充创建人、创建时间等系统字段。
   * 黑名单的审核状态初始化为`null`，需后续审核流程处理。
   *
   * @param dto 黑名单信息DTO，包含黑名单相关字段
   * @param auth 认证信息，用于获取当前操作人ID
   * @return 返回创建后的黑名单实体
   */
  @Api
  @RequirePermission("admin:manage")
  @PostMapping("admin")
  fun postBlackListAsAdmin(@RequestBody dto: BlackListAdminPostDto, @ApiIgnore auth: AuthRequestInfo): BlackList {
    return blackListService.postBlackList(
      createUserAccountId = auth.userId, dto = dto
    )
  }

  /**
   * ## 获取黑名单列表（管理员视图）
   * > 该接口仅限管理员权限访问，返回黑名单的管理员视图数据
   *
   * 通过分页查询参数获取黑名单列表，并使用 `BlackListAdminView` 视图进行数据转换
   * @param pq 分页查询参数，默认值为 `Pq.DEFAULT_MAX`
   * @return 包含黑名单管理员视图的分页结果 `Pr<BlackListAdminView>`
   */
  @Api
  @RequirePermission("admin:manage")
  @GetMapping("admin")
  fun getBlackListsAsAdmin(pq: Pq = Pq.DEFAULT_MAX): Pr<BlackListAdminView> {
    return blackListRepo.findAllBySpec(
      pq = pq, fetcher = BlackListAdminView::class.toFetcher()
    ).transferTo { BlackListAdminView(base = it) }
  }

  /**
   * ## 个人事件文档
   * @param spec 查询条件，包含黑名单事件文档的过滤条件
   * @return 符合条件的黑名单事件文档视图，如果未找到则返回null
   */
  @Api
  @GetMapping("personal_event_doc")
  fun getPersonalEventDoc(spec: BlackListEventDocSpec): BlackListEventDocView? {
    return blackListRepo.sql.createQuery(BlackList::class) {
      where(spec)
      select(table.fetch(BlackListEventDocView::class))
    }.limit(1).offset(0).execute().firstOrNull()
  }

  /**
   * ## 更新黑名单审核状态
   * > 管理员权限下更新指定黑名单的审核状态
   *
   * 该接口仅允许具有ADMIN权限的用户调用，用于更新黑名单的审核状态。
   * 更新操作通过Jimmer DSL实现，确保类型安全。
   *
   * @param id 黑名单记录的唯一标识符，类型为RefId
   * @param auditStatus 要更新的审核状态，类型为AuditTyping枚举
   * @return 更新后的黑名单实体对象，如果更新失败则返回null
   */
  @Api
  @RequirePermission("admin:manage")
  @PatchMapping("audit_status/{id}")
  fun patchAuditStatusByIdAsAdmin(@PathVariable id: RefId, @RequestParam auditStatus: AuditTyping): BlackList? {
    return blackListRepo.update(
      BlackList {
        this.id = id
        this.auditStatus = auditStatus
      })
  }
}
