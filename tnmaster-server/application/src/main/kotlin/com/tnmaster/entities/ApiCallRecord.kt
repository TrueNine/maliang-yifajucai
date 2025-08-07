package com.tnmaster.entities

import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity

/** 请求记录 */
@Entity
interface ApiCallRecord : IEntity {
  /** 请求协议 */
  val reqProtocol: String?

  /** 响应时间 */
  val respDatetime: datetime?

  /** 请求时间 */
  val reqDatetime: datetime?

  /** 请求方法 */
  val reqMethod: String?

  /** 请求路径 */
  val reqPath: String?

  /**
   * 设备 id
   * - user agent
   */
  val deviceCode: String?

  /**
   * 返回结果
   *
   * （json 加密掩码后）
   */
  val respResultEnc: String?

  /** 响应状态码 */
  val respCode: Int?

  /** 请求 ip */
  val reqIp: String?

  /** 登录 ip */
  val loginIp: String?
}
