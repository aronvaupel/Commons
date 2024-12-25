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
@Suppress("UNCHECKED_CAST")
class ReflectionService(
    private val redisService: RedisService,
) {

    fun getClassMemberProperties(declaringClass: KClass<*>): Collection<KProperty1<out Any, *>> {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "getMemberProperties",
                listOf(declaringClass),
                object : TypeReference<Collection<KProperty1<out Any, *>>>() {}
            ) as Collection<KProperty1<out Any, *>>
        } catch (e: NotCachedException) {
            val computed = declaringClass.memberProperties
            redisService.cacheMethodResult(
                "getMemberProperties",
                listOf(declaringClass),
                computed
            )
            computed
        }
    }

    fun <T : BaseEntity> getEntityMemberProperties(entity: T?): List<KProperty1<T, *>>? {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "getMemberProperties",
                listOf(entity),
                object : TypeReference<List<KProperty1<T, *>>>() {}
            ) as List<KProperty1<T, *>>
        } catch (e: NotCachedException) {
            val computed =  entity?.let { entity::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }
            redisService.cacheMethodResult(
                "getMemberProperties",
                listOf(entity),
                computed
            )
            computed
        }
    }

    fun <T : BaseEntity> findMutableProperties(entity: T): Map<String, KMutableProperty<*>> {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "findMutableMemberProperties",
                listOf(entity),
                object : TypeReference<Map<String, KMutableProperty<*>>>() {}
            ) as Map<String, KMutableProperty<*>>
        } catch (e: NotCachedException) {
            val computed =
                entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }
            redisService.cacheMethodResult(
                "findMutableMemberProperties",
                listOf(entity),
                computed
            )
            computed
        }
    }

    fun <T : BaseEntity> findConstructorWithArgs(clazz: KClass<T>): KFunction<T> {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "findConstructorWithArgs",
                listOf(clazz),
                object : TypeReference<KFunction<T>>() {}
            ) as KFunction<T>
        } catch (e: NotCachedException) {
            val computed = clazz.constructors.find { it.parameters.isNotEmpty() }
                ?: throw IllegalArgumentException("No suitable constructor found for ${clazz.simpleName}")
            redisService.cacheMethodResult(
                "findConstructorWithArgs",
                listOf(clazz),
                computed
            )
            computed
        }
    }

    fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "extractFieldTypesMap",
                listOf(entityClass),
                object : TypeReference<Map<String, KType>>() {}
            ) as Map<String, KType>
        } catch (e: NotCachedException) {
            val computed = entityClass.memberProperties.associate { property ->
                property.name to property.returnType
            }
            redisService.cacheMethodResult(
                "extractFieldTypesMap",
                listOf(entityClass),
                computed
            )
            computed
        }
    }

    fun <T : BaseEntity> getConstructorParams(entityConstructor: KFunction<T>): List<KParameter> {
        return try {
            redisService.getCachedMethodResultOrThrow(
                "getConstructorParams",
                listOf(entityConstructor),
                object : TypeReference<List<KParameter>>() {}
            ) as List<KParameter>
        } catch (e: NotCachedException) {
            entityConstructor.parameters
        }
    }

    fun <T : BaseEntity> copy(entity: T): BaseEntity {
        val log = KotlinLogging.logger {}
        return try {
            redisService.getCachedMethodResultOrThrow(
                "copy",
                listOf(entity),
                object : TypeReference<BaseEntity>() {}
            ) as BaseEntity
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
            newInstance
        }
    }

}