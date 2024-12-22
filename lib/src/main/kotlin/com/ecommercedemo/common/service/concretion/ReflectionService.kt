package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.CachingEligible
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Service
open class ReflectionService {

    @CachingEligible
    fun getMemberProperties(declaringClass: KClass<*>) = declaringClass.memberProperties

    @CachingEligible
    private fun <T : BaseEntity> getMemberProperties(clazz: T): Collection<KProperty1<out T, *>> {
        val memberProperties = clazz::class.memberProperties
        return memberProperties
    }

    @CachingEligible
    fun <T : BaseEntity> getMemberProperties(entity: T?): List<KProperty1<T, *>>? {
        val properties =
            entity?.let { entity::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }
        return properties
    }

    @CachingEligible
    fun <T : BaseEntity> findMutableMemberProperties(entity: T): Map<String, KMutableProperty<*>> {
        val entityProperties =
            entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }
        return entityProperties
    }

    @CachingEligible
    fun <T : BaseEntity> findConstructorWithArgs(clazz: KClass<T>): KFunction<T> {
        val entityConstructor = clazz.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No suitable constructor found for ${clazz.simpleName}")
        return entityConstructor
    }

    @CachingEligible
    fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        val result = entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
        return result
    }

    @CachingEligible
    fun <T : BaseEntity> getConstructorParams(entityConstructor: KFunction<T>) = entityConstructor.parameters

    @CachingEligible
    fun <T : BaseEntity> copy(entity: T): BaseEntity {
        val log = KotlinLogging.logger {}
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

        return newInstance
    }

}