package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

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

}
