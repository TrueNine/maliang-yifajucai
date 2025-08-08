package com.tnmaster.repositories

import com.tnmaster.entities.ApiCallRecord
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IApiCallRecordRepo : IRepo<ApiCallRecord, RefId>
