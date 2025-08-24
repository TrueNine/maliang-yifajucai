package com.tnmaster.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.truenine.composeserver.depend.jackson.autoconfig.JacksonAutoConfiguration
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import org.babyfish.jimmer.jackson.ImmutableModule
import org.babyfish.jimmer.sql.kt.cfg.KCustomizer
import org.babyfish.jimmer.sql.kt.cfg.KSqlClientDsl
import org.springframework.stereotype.Component

@Component
class JimmerConfig : KCustomizer {
  @Resource(name = JacksonAutoConfiguration.DEFAULT_OBJECT_MAPPER_BEAN_NAME)
  private lateinit var mapper: ObjectMapper
  
  @Resource(name = JacksonAutoConfiguration.NON_IGNORE_OBJECT_MAPPER_BEAN_NAME) 
  private lateinit var nonMapper: ObjectMapper
  
  @PostConstruct
  fun configureJimmerObjectMappers() {
    val immModule = ImmutableModule()
    mapper.registerModules(immModule)
    nonMapper.registerModules(immModule)
  }

  override fun customize(dsl: KSqlClientDsl) {
    dsl.setDefaultSerializedTypeObjectMapper(mapper)
    dsl.setDefaultBinLogObjectMapper(mapper)
  }
}
