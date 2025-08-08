package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckLogin
import com.tnmaster.dto.jobseeker.JobSeekerAdminSpec
import com.tnmaster.entities.JobSeeker
import com.tnmaster.entities.by
import com.tnmaster.repositories.IJobSeekerRepo
import org.babyfish.jimmer.client.FetchBy
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** 第二版求职者API接口 */
@Api
@RestController
@RequestMapping("v2/jobSeeker")
class JobSeekerV2Api(private val jobSeekerRepo: IJobSeekerRepo) {
  /** 后台筛选求职者 */
  @Api
  @SaCheckLogin
  @GetMapping("admin")
  fun getAdminJobSeekerList(spec: JobSeekerAdminSpec): List<@FetchBy("ADMIN_JOB_SEEKER") JobSeeker> {
    return jobSeekerRepo.sql
      .createQuery(JobSeeker::class) {
        where(spec)
        select(table.fetch(ADMIN_JOB_SEEKER))
      }
      .execute()
  }

  companion object {
    val ADMIN_JOB_SEEKER =
      newFetcher(JobSeeker::class).by {
        rqSocial()
        rqGotoWork()
        exAddressCode()
        regAddressCode()
        userAccount {
          account()
          nickName()
          createUserAccount()
        }
        userInfo {
          phone()
          sparePhone()
          email()
          disInfo {
            certCode()
            dsType()
            level()
            certCode()
          }
        }
      }
  }
}
