package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.entities.CommonKvConfigDbCache
import com.tnmaster.service.CommonKvConfigDbCacheService
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.Pr
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.*

@Api
@RestController
@RequestMapping("v2/server_cache_config")
class ServerCacheConfigV2Api(private val cacheService: CommonKvConfigDbCacheService) {

  /** ## 删除百家姓 */
  @Api
  @DeleteMapping("china_first_name")
  fun deleteChinaFirstName(@RequestParam name: String) {
    cacheService.removeChinaFirstName(name)
  }

  /** ## 添加百家姓 */
  @Api
  @SaCheckPermission("ADMIN")
  @PatchMapping("china_first_name")
  fun patchChinaFirstName(@Valid @Size(min = 1, max = 2, message = "长度不正确") @NotBlank(message = "姓不能为空") @RequestParam name: String): Set<String> {
    return cacheService.postChinaFirstName(name)
  }

  /** ## 获取所有中国百家姓 */
  @Api
  @GetMapping("china_first_names")
  fun getChinaFirstNames(): Set<String> {
    return cacheService.fetchChinaFirstNames()
  }

  /**
   * ## 获取所有缓存配置数据
   *
   * @param pq 分页参数
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("/")
  fun getAllCacheConfigData(pq: Pq = Pq.DEFAULT_MAX): Pr<CommonKvConfigDbCache> {
    return cacheService.fetchAll()
  }
}
