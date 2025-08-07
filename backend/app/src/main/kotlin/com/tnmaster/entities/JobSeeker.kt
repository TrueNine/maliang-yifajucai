package  com.tnmaster.entities

import com.tnmaster.entities.embeddable.ExSalaryRange
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.DegreeTyping
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne

@Entity
interface JobSeeker : IEntity {
  @IdView("userAccount")
  val userAccountId: RefId?

  /** 用户账号 */
  @ManyToOne
  @JoinColumn(name = "user_id")
  val userAccount: UserAccount?

  /** 需要到达的地方 */
  val exAddressCode: String?

  /** 接受远程工作 */
  val exRemote: Boolean?

  /** 需要社保 */
  val rqSocial: Boolean?

  @IdView("exIndustry")
  val exIndustryId: RefId?

  /** 期望行业 */
  @ManyToOne
  @JoinColumn
  val exIndustry: Industry?

  /** 当前户籍所在地 */
  val regAddressCode: String?

  /** 想要的工资范围 */
  val salaryRange: ExSalaryRange?

  /** 简历创建时间 */
  val createDatetime: datetime

  /** 学历 */
  val degree: DegreeTyping?

  /** 愿意去上班 */
  val rqGotoWork: Boolean?

  /** 残疾就职特殊简历 */
  @OneToOne(mappedBy = "jobSeeker")
  val jobSeekerDisNominal: JobSeekerDisNominal?

  /** 该简历的入职状态 */
  @OneToOne(mappedBy = "jobSeeker")
  val jobSeekerStatus: JobSeekerStatus?

  @IdView("userInfo")
  val userInfoId: RefId?

  /** 该简历所属的用户信息 */
  @OneToOne
  @JoinColumn(name = "user_info_id")
  val userInfo: UserInfo?

  /** 所有的个税视频 */
  @OneToMany(mappedBy = "jobSeeker")
  val taxVideos: List<JobSeekerDisNominalTaxVideo>
}
