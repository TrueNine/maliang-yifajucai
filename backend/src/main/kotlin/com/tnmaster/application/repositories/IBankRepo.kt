package com.tnmaster.application.repositories

import com.tnmaster.entities.Bank
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IBankRepo : IRepo<Bank, RefId>
