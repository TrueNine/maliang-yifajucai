package com.tnmaster.application.rules

import com.yomahub.liteflow.core.NodeComponent
import com.yomahub.liteflow.flow.LiteflowResponse
import kotlin.reflect.KClass

fun <T : Any> NodeComponent.getContextBean(clazz: KClass<T>): T? {
  return getContextBean(clazz.java)
}

inline fun <reified T> NodeComponent.getContextBean(): T? {
  return getContextBean(T::class.java)
}

fun LiteflowResponse.orThrow(): LiteflowResponse {
  return this.apply {
    if (!isSuccess) {
      cause?.also { throw it }
    }
  }
}
