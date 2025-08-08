package com.tnmaster.application.controller

import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.dto.address.AddressFullPathView
import com.tnmaster.entities.Address
import com.tnmaster.application.repositories.IAddressRepo
import com.tnmaster.application.service.AddressService
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** ## 第2版地址 API */
@Api
@RestController
@RequestMapping("v2/address")
class AddressV2Api(
  private val repo: IAddressRepo,
  private val addressService: AddressService,
) {
  /** ## 获取直接的地址直接子集 */
  @Api
  @GetMapping("direct_children")
  fun getDirectChildrenByCode(@RequestParam code: String): List<Address> {
    return addressService.fetchDirectChildrenByCode(code = code)
  }

  /**
   * ## 批量获取地址全路径
   *
   * @param codes 批量地址代码
   */
  @Api
  @GetMapping("full_paths/codes")
  fun getFullPathsByCodes(@RequestParam codes: List<String>): List<AddressFullPathView> {
    require(codes.size <= 100) { "codes.size must be less than or equal to 100" }
    return repo.findAllFullPathByCodesIn(codes)
  }

  /** ## 查询所有省份 */
  @Api
  @GetMapping("provinces")
  fun getAllProvinces(): List<Address> {
    return addressService.fetchProvince()
  }

  /**
   * ## 初始化省份
   * TODO ROOT 权限
   */
  @Api
  @SaCheckPermission("ROOT")
  @PostMapping("init_provinces")
  fun postInitProvincesAsAdmin() {
    addressService.initProvince()
  }
}
