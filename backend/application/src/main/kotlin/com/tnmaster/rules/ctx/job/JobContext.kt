package com.tnmaster.rules.ctx.job

import com.tnmaster.entities.*
import io.github.truenine.composeserver.RefId

data class JobContext(
  val jobId: RefId,
  val userId: RefId,
  var isBlackList: Boolean = false,
  var userInfo: UserInfo? = null,
  var user: UserAccount? = null,
  var jobSeeker: JobSeeker? = null,
  var disInfo: DisInfo? = null,
  var job: Job? = null,
)
