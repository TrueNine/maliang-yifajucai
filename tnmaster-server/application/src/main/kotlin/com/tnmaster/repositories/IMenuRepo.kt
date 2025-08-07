package com.tnmaster.repositories

import com.tnmaster.entities.Menu
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IMenuRepo : IRepo<Menu, RefId>
