package com.tnmaster.service

import com.tnmaster.entities.ApiCallRecord
import com.tnmaster.repositories.IApiCallRecordRepo
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
        log.info("DEBUG: RedisTemplate的valueSerializer: {}", redisTemplate.valueSerializer?.javaClass?.name)
        redisTemplate.expire(CACHE_KEY, CACHE_DURATION)
        redisTemplate.opsForSet().add(CACHE_KEY, record)
      }
        .await()
    }
  }

  /** 调度任务，过一段时间，统一写入数据库 */
  @Scheduled(fixedDelay = 1000 * 60 * 2, initialDelay = 1000 * 60)
  fun scheduledCacheSaveToDatabase() {
    val cacheData = redisTemplate.opsForSet().members(CACHE_KEY)?.filterNotNull()
    if (null != cacheData && cacheData.isNotEmpty()) {
      cacheData.forEach { record ->
        jimmerApiCallRecordRepo.sql.save(record, SaveMode.INSERT_IF_ABSENT)
      }
      clearCachedRecords()
    }
  }

  fun clearCachedRecords() {
    redisTemplate.delete(CACHE_KEY)
  }
}
