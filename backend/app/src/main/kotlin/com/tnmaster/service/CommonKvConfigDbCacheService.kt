package com.tnmaster.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.tnmaster.entities.CommonKvConfigDbCache
import com.tnmaster.repositories.ICommonKvConfigDbCacheRepo
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.Pr
import io.github.truenine.composeserver.data.extract.service.IChinaFirstNameService
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.toPageable
import io.github.truenine.composeserver.rds.toPr
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class CommonKvConfigDbCacheService(private val jsonMapper: ObjectMapper, private val cacheRepo: ICommonKvConfigDbCacheRepo) {
  object Keys {
    const val CHINA_FIRST_NAMES = "data.name.china.first"
  }

  fun fetchAll(pq: Pq? = Pq.DEFAULT_MAX): Pr<CommonKvConfigDbCache> {
    return cacheRepo.findAll(pq.toPageable()).toPr()
  }

  operator fun get(k: String): String? {
    return cacheRepo.findFirstValueByKey(k)
  }

  operator fun <T : Any> get(k: String, type: KClass<T>): T? {
    return cacheRepo.findFirstValueByKey(k)?.let { jsonMapper.readValue(it, type.java) }
  }

  @ACID
  fun postChinaFirstName(name: String, k: String = Keys.CHINA_FIRST_NAMES): Set<String> {
    val names = fetchChinaFirstNames(k).toMutableSet()
    names += name
    return names.apply { postString(k = k, v = jsonMapper.writeValueAsString(names)) }
  }

  @ACID
  fun removeChinaFirstName(name: String, k: String = Keys.CHINA_FIRST_NAMES) {
    val e = cacheRepo.findFirstValueByKey(k)?.let { jsonMapper.readValue(it, Set::class.java) }?.map { it.toString() }?.toSet()
    e?.apply { filter { it != name }.also { post(k, it) } }
  }

  fun fetchChinaFirstNames(k: String = Keys.CHINA_FIRST_NAMES): Set<String> {
    return cacheRepo.findFirstValueByKey(k)?.let { jsonMapper.readValue(it, Set::class.java) }?.map { it.toString() }?.toSet()
      ?: run {
        val list = IChinaFirstNameService.CHINA_FIRST_NAMES.toSet()
        post(k, list)
      }
  }

  /** ## 保存 object value */
  @ACID
  fun <T : Any> post(k: String, v: T): T {
    postString(k, jsonMapper.writeValueAsString(v))
    return v
  }

  /** ## 保存 string value */
  @ACID
  fun postString(k: String, v: String? = null): String? {
    return cacheRepo
      .save(
        CommonKvConfigDbCache {
          this.k = k
          this.v = v
        }
      )
      .v
  }
}
