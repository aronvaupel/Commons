package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.SpringContextProvider
import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.NotCachedException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@Service
class TypeReAttacher {
    private val redisService: RedisService by lazy {
        SpringContextProvider.applicationContext.getBean(RedisService::class.java)
    }
    val log = mu.KotlinLogging.logger {}
    private fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        log.info { "Uses extractFieldTypesMap" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "extractFieldTypesMap",
                listOf(entityClass),
                object : TypeReference<Map<String, KType>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val computed = entityClass.memberProperties.associate { property ->
                property.name to property.returnType
            }
            redisService.cacheMethodResult(
                "extractFieldTypesMap",
                listOf(entityClass),
                computed
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            computed
        }
    }

    fun reAttachType(
        data: Map<String, Any?>,
        entityClass: KClass<*>
    ): Map<String, Any?> {


        val typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }

        val typedData: Map<String, Any?> = typesForDataKeys.mapValues { (key, kType) ->
            val typeReference = object : TypeReference<Any>() {
                override fun getType() = kType.javaType
            }
            SpringContextProvider.applicationContext.getBean(ObjectMapper::class.java)
                .convertValue(data[key], typeReference)
        }
        return typedData
    }

}
