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
  @Qualifier("yfjc_redisTemplate") private val redisTemplate: RedisTemplate<String, ApiCallRecord?>,
) {
  companion object {
    private val log = logger<ApiCallRecordService>()
    const val CACHE_KEY = "apiCallRecord"
    val CACHE_DURATION: Duration = Duration.ofHours(3)
  }

  suspend fun postToCache(@Valid record: ApiCallRecord) {
    withContext(Dispatchers.IO) {
      async {
        log.info("Storing API call record to cache - method: {}, path: {}, respCode: {}", 
          record.reqMethod, record.reqPath, record.respCode)
        redisTemplate.expire(CACHE_KEY, CACHE_DURATION)
        redisTemplate.opsForSet().add(CACHE_KEY, record)
      }
        .await()
    }
  }

  /** 调度任务，过一段时间，统一写入数据库 */
  @Scheduled(fixedDelay = 1000 * 60 * 2, initialDelay = 1000 * 60)
  fun scheduledCacheSaveToDatabase() {
    try {
      log.info("Scheduled task: attempting to process cached API call records")

      // 检查 Redis 连接健康状态
      if (!checkRedisHealth()) {
        log.warn("Redis connection unhealthy, skipping scheduled task")
        return
      }

      val cacheData = redisTemplate.opsForSet().members(CACHE_KEY)?.filterNotNull()

      if (null != cacheData && cacheData.isNotEmpty()) {
        log.info("Processing {} cached API call records", cacheData.size)
        cacheData.forEach { record ->
          when (record) {
            else -> {
              log.debug("保存正常的ApiCallRecord到数据库")
              jimmerApiCallRecordRepo.sql.save(record, SaveMode.INSERT_IF_ABSENT)
            }
          }
        }
        clearCachedRecords()
        log.info("Successfully processed and cleared cached API call records")
      } else {
        log.debug("No cached API call records to process")
      }
    } catch (ex: Exception) {
      log.error("Scheduled task failed: unable to process cached API call records - {}", ex.message, ex)
    }
  }

  /**
   * 检查 Redis 连接健康状态
   */
  private fun checkRedisHealth(): Boolean {
    return try {
      redisTemplate.hasKey("health-check") // 简单的连接测试
      log.debug("Redis health check passed")
      true
    } catch (ex: Exception) {
      log.error("Redis health check failed: {}", ex.message)
      false
    }
  }

  fun clearCachedRecords() {
    redisTemplate.delete(CACHE_KEY)
  }
}
