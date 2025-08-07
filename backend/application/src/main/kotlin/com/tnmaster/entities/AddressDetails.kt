package com.tnmaster.entities

import com.tnmaster.entities.converter.NameSensitiveJsonConverter
import com.tnmaster.entities.converter.PhoneSensitiveJsonConverter
import com.tnmaster.entities.resolvers.AddressDetailsAddressResolver
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.*

/**
 * 地址详情
 */
@Entity
interface AddressDetails : IEntity {
  @IdView("userAccount")
  val userAccountId: RefId

  @ManyToOne
  @JoinColumn(name = "user_id")
  val userAccount: UserAccount

  @JsonConverter(PhoneSensitiveJsonConverter::class)
  val phone: String?

  @Formula(dependencies = ["phone"])
  val metadataPhone: String? get() = phone

  @JsonConverter(NameSensitiveJsonConverter::class)
  val name: String?

  @Formula(dependencies = ["name"])
  val metadataName: String? get() = name

  val addressCode: String

  @Transient(AddressDetailsAddressResolver::class)
  val address: Address

  val addressDetails: String
}
