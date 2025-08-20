package com.tnmaster.service

import com.tnmaster.entities.ApiCallRecord
import com.tnmaster.repositories.IApiCallRecordRepo
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.logger
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class ApiCallRecordService(
  private val jimmerApiCallRecordRepo: IApiCallRecordRepo,
  @Qualifier("apiCallRecordRedisTemplate") private val redisTemplate: RedisTemplate<String, ApiCallRecord?>,
) {
  companion object {
    private val log = logger<ApiCallRecordService>()
    const val CACHE_KEY = "apiCallRecord"
    val CACHE_DURATION: Duration = Duration.ofHours(3)
  }

  suspend fun postToCache(@Valid record: ApiCallRecord) {
    withContext(Dispatchers.IO) {
      async {
        log.info("DEBUG: 使用的RedisTemplate: {}", redisTemplate::class.java.name)
        log.info("DEBUG: RedisTemplate的valueSerializer: {}", redisTemplate.valueSerializer.javaClass.name)
        redisTemplate.expire(CACHE_KEY, CACHE_DURATION)
        redisTemplate.opsForSet().add(CACHE_KEY, record)
      }
        .await()
    }
  }

  /** 调度任务，过一段时间，统一写入数据库 */
  @Scheduled(fixedDelay = 1000 * 60 * 2, initialDelay = 1000 * 60)
  fun scheduledCacheSaveToDatabase() {
    // 使用主RedisTemplate以获得错误处理功能
    val mainRedisTemplate = redisTemplate as RedisTemplate<String, Any?>
    val cacheData = mainRedisTemplate.opsForSet().members(CACHE_KEY)?.filterNotNull()

    if (null != cacheData && cacheData.isNotEmpty()) {
      cacheData.forEach { record ->
        when (record) {
          is ApiCallRecord -> {
            log.debug("保存正常的ApiCallRecord到数据库")
            jimmerApiCallRecordRepo.sql.save(record, SaveMode.INSERT_IF_ABSENT)
          }

          is Map<*, *> -> {
            log.warn("遇到反序列化错误的数据，尝试手动重建ApiCallRecord对象")
            try {
              val reconstructedRecord = reconstructApiCallRecordFromMap(record)
              jimmerApiCallRecordRepo.sql.save(reconstructedRecord, SaveMode.INSERT_IF_ABSENT)
            } catch (ex: Exception) {
              log.error("重建ApiCallRecord失败，跳过此记录", ex)
            }
          }

          else -> {
            log.warn("遇到未知类型的缓存数据: {}, 类型: {}", record, record.javaClass.simpleName)
          }
        }
      }
      clearCachedRecords()
    }
  }

  /**
   * 从Map重建ApiCallRecord对象
   */
  private fun reconstructApiCallRecordFromMap(map: Map<*, *>): ApiCallRecord {
    return ApiCallRecord {
      reqProtocol = map["reqProtocol"] as? String
      reqMethod = map["reqMethod"] as? String
      reqPath = map["reqPath"] as? String
      reqDatetime = (map["reqDatetime"] as? String)?.let { datetime.parse(it) }
      respDatetime = (map["respDatetime"] as? String)?.let { datetime.parse(it) }
      respCode = map["respCode"] as? Int
      deviceCode = map["deviceCode"] as? String
      reqIp = map["reqIp"] as? String
      loginIp = map["loginIp"] as? String
      respResultEnc = map["respResultEnc"] as? String
    }
  }

  fun clearCachedRecords() {
    redisTemplate.delete(CACHE_KEY)
  }
}
