package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Service
class ReflectionService {
    val log = KotlinLogging.logger {}

    fun getClassMemberProperties(declaringClass: KClass<*>): Collection<KProperty1<out Any, *>> =
        declaringClass.memberProperties


    fun <T : BaseEntity> getEntityMemberProperties(entity: T?): List<KProperty1<T, *>>? =
        entity?.let { entity::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }


    fun <T : BaseEntity> findMutableProperties(entity: T): Map<String, KMutableProperty<*>> =
        entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }


    fun <T : BaseEntity> findConstructorWithArgs(clazz: KClass<T>): KFunction<T> {
        return clazz.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No suitable constructor found for ${clazz.simpleName}")
    }

    fun <T : BaseEntity> getConstructorParams(entityConstructor: KFunction<T>): List<KParameter> =
        entityConstructor.parameters

    fun <T : BaseEntity> copy(entity: T): BaseEntity {
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