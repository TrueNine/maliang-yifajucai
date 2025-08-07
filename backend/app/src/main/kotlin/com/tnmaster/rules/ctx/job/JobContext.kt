package com.tnmaster.rules.ctx.job

import com.tnmaster.entities.DisInfo
import com.tnmaster.entities.Job
import com.tnmaster.entities.JobSeeker
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
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
