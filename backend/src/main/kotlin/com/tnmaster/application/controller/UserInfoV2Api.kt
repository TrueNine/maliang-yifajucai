package com.tnmaster.application.controller

import cn.dev33.satoken.annotation.SaCheckLogin
import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.dto.cert.CertAdminCertifiedUserInfoSpec
import com.tnmaster.dto.userinfo.UserInfoAdminCertifiedView
import com.tnmaster.dto.userinfo.UserInfoAdminPostDto
import com.tnmaster.dto.userinfo.UserInfoAdminPutDto
import com.tnmaster.dto.userinfo.UserInfoAdminSpec
import com.tnmaster.dto.userinfo.UserInfoAdminView
import com.tnmaster.dto.userinfo.UserInfoMemberView
import com.tnmaster.dto.userinfo.UserInfoPutDto
import com.tnmaster.entities.DisInfo
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.addressCode
import com.tnmaster.entities.by
import com.tnmaster.entities.id
import com.tnmaster.application.repositories.IUserInfoRepo
import com.tnmaster.application.service.UserAccountService
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.rds.jimmerextpostgres.substr
import io.github.truenine.composeserver.rds.toFetcher
import io.github.truenine.composeserver.toId
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * # 用户信息第二版接口
 *
 * @author TrueNine
 * @since 2025-02-24
 */
@Api
@RestController
@RequestMapping("v2/user_info")
class UserInfoV2Api(
  private val userInfoRepo: IUserInfoRepo,
  private val userAccountService: UserAccountService,
) {

  /**
   * ## 获取当前用户的残疾信息
   * > 通过认证信息获取当前用户的残疾信息
   *
   * 该接口用于获取当前登录用户的残疾信息，需要用户已登录。
   *
   * @param authInfo 认证信息，包含用户ID
   * @return 当前用户的残疾信息，如果不存在则返回null
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/dis_info")
  fun getDisInfoAsMe(@ApiIgnore authInfo: AuthRequestInfo): DisInfo? {
    return userInfoRepo.findFirstDisInfoByUserAccountId(userAccountId = authInfo.userId)
  }

  /**
   * ## 获取认证用户信息视图（管理员权限）
   * > 管理员权限下获取认证用户信息的视图
   *
   * 该接口用于管理员权限下获取认证用户信息的视图，需要具备 `ADMIN` 权限。
   *
   * @param spec 查询参数，包含获取认证用户信息所需的条件
   * @return 认证用户信息的视图，如果未找到则返回 `null`
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("certified_user_info")
  fun getCertifiedUserInfoViewAsAdmin(spec: CertAdminCertifiedUserInfoSpec): UserInfoAdminCertifiedView? {
    return userAccountService.fetchUserInfoCertifyViewByUserAccountId(spec)
  }

  /**
   * ## 获取用户实名认证状态
   * > 查询当前登录用户的实名认证状态
   *
   * 该接口用于获取当前登录用户是否已完成实名认证。需要用户已登录状态。
   *
   * @param authInfo 用户认证信息（自动注入，无需手动传递）
   * @return 返回用户实名认证状态（true：已认证，false：未认证）
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/citizen_verified")
  fun getCitizenVerified(@ApiIgnore authInfo: AuthRequestInfo): Boolean {
    return userAccountService.fetchUserInfoIsCitizenVerifiedByUserAccountId(authInfo.userId)
  }

  /**
   * ## 修改当前自身登录的用户信息
   *
   * @param modifyUserInfo 修改的用户信息
   * @return 修改后的用户信息
   */
  @Api
  @SaCheckLogin
  @PutMapping("me")
  fun putUserInfoAsMe(@RequestBody modifyUserInfo: UserInfoPutDto, @ApiIgnore auth: AuthRequestInfo): UserInfo {
    val infoId = userInfoRepo.findFirstByUserAccountIdOrNull(auth.userId, newFetcher(UserInfo::class).by {})?.id
    checkNotNull(infoId) { "未查询到当前用户的用户信息" }
    return userInfoRepo.save(modifyUserInfo.toEntity { this.id = infoId }, SaveMode.UPDATE_ONLY)
  }

  /**
   * ## 根据当前登录用户，查询当前用户信息
   *
   * @param authInfo 当前登录用户信息
   * @return 当前登录用户信息
   */
  @Api
  @SaCheckLogin
  @GetMapping("me")
  fun getUserInfoAsMe(@ApiIgnore authInfo: AuthRequestInfo): UserInfoMemberView? {
    return authInfo.userId.let { userAccountId ->
      userInfoRepo.findFirstByUserAccountIdOrNull(userAccountId, fetcher = UserInfoMemberView::class.toFetcher())?.let { UserInfoMemberView(it) }
    }
  }

  /**
   * ## 管理端保存用户信息
   * > 管理员权限下保存用户信息
   *
   * 该接口用于管理员权限下保存用户信息，需要具备 `ADMIN` 权限。
   *
   * @param dto 用户信息数据传输对象，包含需要保存的用户信息
   * @return 保存后的用户信息
   */
  @Api
  @SaCheckLogin
  @PostMapping("/")
  fun postUserInfoAsAdmin(
    @RequestBody dto: UserInfoAdminPostDto,
    @ApiIgnore authInfo: AuthRequestInfo,
  ): UserInfo {
    return userAccountService.postCustomerUserInfo(authInfo.userId, dto)
  }

  /**
   * ## 根据用户ID获取用户信息（管理员权限）
   * > 通过用户ID查询用户信息，仅限管理员权限访问。
   *
   * 该接口用于管理员根据用户ID查询用户信息。如果用户ID为空，则返回null。
   *
   * @param id 用户ID，类型为RefId
   * @return 返回用户信息视图，类型为UserInfoAdminView?，如果用户ID为空或未找到对应信息，则返回null
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("id/{id}")
  fun getUserInfoByIdAsAdmin(@PathVariable id: RefId): UserInfoAdminView? {
    return userInfoRepo.viewer(UserInfoAdminView::class).findNullable(id)
  }

  /**
   * ## 获取用户信息表单（管理员权限）
   * > 管理员权限下获取用户信息表单数据
   *
   * 该接口用于管理员权限下获取用户信息表单数据，需要用户登录且具有 `ADMIN` 权限。
   *
   * @param spec 用户信息查询条件
   * @return 用户信息表单数据，若不存在则返回 `null`
   */
  @Api
  @SaCheckLogin
  @SaCheckPermission("ADMIN")
  @GetMapping("form")
  fun getUserInfoPutFormAsAdmin(spec: UserInfoAdminSpec): UserInfoAdminPostDto? {
    return userInfoRepo.findFirstBySpec(spec, fetcher = UserInfoAdminPostDto::class.toFetcher())?.let { UserInfoAdminPostDto(it) }
  }

  /**
   * ## 管理端保存用户信息
   * > 管理员权限下保存用户信息
   *
   * 该接口用于管理员权限下保存用户信息，需要具备 `ADMIN` 权限。
   *
   * @param dto 用户信息数据传输对象，包含需要保存的用户信息
   * @return 保存后的用户信息
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PutMapping("/")
  fun putUserInfoAsAdmin(
    @RequestBody dto: UserInfoAdminPutDto,
    @ApiIgnore authInfo: AuthRequestInfo,
  ): UserInfo {
    return userAccountService.modifyCustomerUserInfo(authInfo.userId, dto)
  }

  /**
   * ## 获取当前用户的个人信息表单
   * > 通过认证信息获取当前用户的个人信息表单
   *
   * 该接口用于获取当前登录用户的个人信息表单数据。需要用户已登录才能访问。
   *
   * @param authInfo 认证信息，包含当前登录用户的ID
   * @return 当前用户的个人信息表单数据，如果用户不存在则返回null
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/form")
  fun getUserInfoPutDtoAsMe(@ApiIgnore authInfo: AuthRequestInfo): UserInfo? {
    return userAccountService.fetchPutDtoByUserAccountId(authInfo.userId)
  }

  /**
   * ## 获取用户信息列表（管理员权限）
   * > 管理员权限下获取用户信息列表，支持分页和条件查询
   *
   * 根据传入的查询条件和分页参数，返回符合条件的分页用户信息列表。
   *
   * @param spec 用户信息查询条件，可为空
   * @return 分页后的用户信息列表
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("user_infos")
  fun getUserInfosAsAdmin(spec: UserInfoAdminSpec): IPage<UserInfoAdminView> {
    return userAccountService.fetchUserInfosAsAdmin(spec)
  }

  /**
   * ## 获取用户信息详情（管理员权限）
   * > 通过用户信息ID查询用户信息详情，仅限管理员权限访问。
   *
   * 该接口用于管理员根据用户信息ID查询用户信息详情。如果用户信息ID为空，则返回null。
   *
   * @param userInfoId 用户信息ID，类型为RefId
   * @return 返回用户信息视图，类型为UserInfoAdminView?，如果用户信息ID为空或未找到对应信息，则返回null
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("user_info_details")
  fun getUserInfoDetailsByUserInfoIdAsAdmin(@RequestParam userInfoId: RefId?): UserInfoAdminView? {
    if (userInfoId == null) return null
    return userInfoRepo.sql.createQuery(UserInfo::class) {
      where(table.id eq userInfoId.toId())
      select(table.fetch(UserInfoAdminView::class))
    }.fetchOneOrNull()
  }

  /**
   * ## 查询各省份的用户数量
   *
   * @param spec 查询条件
   * @return 省份和用户数量
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("province_user_info_count")
  fun getUserCountByProvinceAsAdmin(spec: UserInfoAdminSpec): Map<String, Long> {
    return userInfoRepo.sql.createQuery(UserInfo::class) {
      where(table.addressCode.isNotNull())
      groupBy(table.addressCode.substr(1, 2))
      select(table.addressCode.substr(1, 2), count(table.id, distinct = true))
    }.execute().associate { it._1 to it._2 }.toMap()
  }
}
