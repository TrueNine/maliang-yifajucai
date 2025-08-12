package com.tnmaster.config.redis

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import io.github.truenine.composeserver.logger
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.stereotype.Component

/**
 * Redis序列化错误处理器
 * 
 * 处理Redis序列化/反序列化过程中的