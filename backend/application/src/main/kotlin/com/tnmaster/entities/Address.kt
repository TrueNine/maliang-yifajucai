package com.tnmaster.entities

import com.tnmaster.entities.resolvers.AddressParentFullNameResolver
import com.tnmaster.entities.resolvers.AddressParentPathResolver
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Transient

@Entity
interface Address : IEntity {
  @Key
  val code: String
  val name: String
  val yearVersion: Int?
  val level: Int
  val center: String?
  val leaf: Boolean?

  /** 当前地址的全路径 */
  @Transient(AddressParentFullNameResolver::class)
  val fullPath: String?

  /** 直接的父 path */
  @Transient(AddressParentPathResolver::class)
  val parentAddresses: List<Address>

  /** 上一级地址 */
  @ManyToOne
  @JoinColumn(name = "rpi")
  val parentAddress: Address?

  @IdView("parentAddress")
  val rpi: RefId?
}
