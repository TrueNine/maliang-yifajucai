package com.tnmaster.service

import com.tnmaster.entities.Api
import com.tnmaster.repositories.IApiRepo
import io.github.truenine.composeserver.logger
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.stereotype.Service

@Service
class ApiService(private val apiRepo: IApiRepo) : IApiRepo by apiRepo {
  companion object {
    @JvmStatic
    private val log = logger<ApiService>()
  }

  fun postAllFound(apis: List<Api>): List<Api> {
    return apiRepo.saveEntitiesCommand(apis, SaveMode.UPSERT).execute().items.map { it.modifiedEntity }
  }
}
