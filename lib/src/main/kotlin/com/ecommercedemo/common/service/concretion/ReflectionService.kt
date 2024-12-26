package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.NotCachedException
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Service
class ReflectionService(
    private val redisService: RedisService,
) {
    val log = KotlinLogging.logger {}

    fun getClassMemberProperties(declaringClass: KClass<*>): Collection<KProperty1<out Any, *>> {
        log.info { "Uses getClassMemberProperties" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "getMemberProperties",
                listOf(declaringClass),
                object : TypeReference<Collection<KProperty1<out Any, *>>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val computed = declaringClass.memberProperties
            redisService.cacheMethodResult(
                "getMemberProperties",
                listOf(declaringClass),
                computed
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            computed
        }
    }

    fun <T : BaseEntity> getEntityMemberProperties(entity: T?): List<KProperty1<T, *>>? {
        log.info { "Uses getEntityMemberProperties" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "getMemberProperties",
                listOf(entity),
                object : TypeReference<List<KProperty1<T, *>>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val computed = entity?.let { entity::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }
            redisService.cacheMethodResult(
                "getMemberProperties",
                listOf(entity),
                computed
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            computed
        }
    }

    fun <T : BaseEntity> findMutableProperties(entity: T): Map<String, KMutableProperty<*>> {
        log.info { "Uses findMutableProperties" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "findMutableMemberProperties",
                listOf(entity),
                object : TypeReference<Map<String, KMutableProperty<*>>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val computed =
                entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }
            redisService.cacheMethodResult(
                "findMutableMemberProperties",
                listOf(entity),
                computed
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            computed
        }
    }

    fun <T : BaseEntity> findConstructorWithArgs(clazz: KClass<T>): KFunction<T> {
        log.info { "Saved to cache" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "findConstructorWithArgs",
                listOf(clazz),
                object : TypeReference<KFunction<T>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val computed = clazz.constructors.find { it.parameters.isNotEmpty() }
                ?: throw IllegalArgumentException("No suitable constructor found for ${clazz.simpleName}")
            redisService.cacheMethodResult(
                "findConstructorWithArgs",
                listOf(clazz),
                computed
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            computed
        }
    }

    fun <T : BaseEntity> getConstructorParams(entityConstructor: KFunction<T>): List<KParameter> {
        log.info { "Uses getConstructorParams" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "getConstructorParams",
                listOf(entityConstructor),
                object : TypeReference<List<KParameter>>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val result = entityConstructor.parameters
            redisService.cacheMethodResult(
                "getConstructorParams",
                listOf(entityConstructor),
                entityConstructor.parameters
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            result
        }
    }

    fun <T : BaseEntity> copy(entity: T): BaseEntity {
        log.info { "Uses copy" }
        val start = System.currentTimeMillis()
        return try {
            val fromCache = redisService.getCachedMethodResultOrThrow(
                "copy",
                listOf(entity),
                object : TypeReference<BaseEntity>() {}
            )
            log.info { "Got from cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            fromCache
        } catch (e: NotCachedException) {
            val constructor = entity::class.primaryConstructor
                ?: throw IllegalStateException("No primary constructor for ${entity::class.simpleName}")

            val args = constructor.parameters.associateWith { param ->
                val property = entity::class.memberProperties.firstOrNull { it.name == param.name }
                    ?: throw IllegalArgumentException("No property found for parameter '${param.name}'")

                try {
                    property.getter.apply { isAccessible = true }.call(this)
                } catch (e: IllegalAccessException) {
                    val publicGetter =
                        entity::class.memberProperties.firstOrNull { it.name == param.name?.removePrefix("_") }
                    publicGetter?.getter?.call(this)
                }
            }

            val newInstance = constructor.callBy(args)

            this::class.memberProperties
                .filter { property -> constructor.parameters.none { it.name == property.name } }
                .forEach { property ->
                    try {
                        val value = when {
                            property.name.startsWith("_") -> {
                                val publicGetterName = property.name.removePrefix("_")
                                entity::class.memberProperties
                                    .firstOrNull { it.name == publicGetterName }
                                    ?.getter
                                    ?.call(this)
                            }

                            else -> property.getter.call(this)
                        }
                        if (property is KMutableProperty<*>) {
                            property.setter.call(newInstance, value)
                        }
                    } catch (e: Exception) {
                        log.warn("Failed to copy property: ${e.message}")
                        log.debug { e.stackTraceToString() }
                    }
                }
            redisService.cacheMethodResult(
                "copy",
                listOf(entity),
                newInstance
            )
            log.info { "Saved to cache" }
            val end = System.currentTimeMillis()
            log.info { "Time elapsed: ${end - start} ms" }
            newInstance
        }
    }

}