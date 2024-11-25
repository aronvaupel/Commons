package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.EntityChangeTracker
import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.persistence.concretion.PseudoPropertyRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
abstract class ServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val entityClass: KClass<T>,
    private val eventProducer: EntityEventProducer,
    private val objectMapper: ObjectMapper,
    private val pseudoPropertyRepository: PseudoPropertyRepository,
    private val retriever: Retriever
) : IService<T> {

    @Transactional
    override fun create(request: CreateRequest<T>): T {
        val requestPropertyMap = request.data::class.memberProperties.associateBy { it.name }

        val entityConstructor = entityClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No suitable constructor found for ${entityClass.simpleName}")

        val entityConstructorParams = entityConstructor.parameters.associateWith { param ->
            val requestProperty = requestPropertyMap[param.name]
            val value = requestProperty?.getter?.call(request.data)

            if (value == null && !param.type.isMarkedNullable) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
            value
        }

        val newInstance = entityConstructor.callBy(entityConstructorParams)

        val targetPropertyMap = newInstance::class.memberProperties.associateBy { it.name }

        targetPropertyMap.values
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { property ->
                property.isAccessible = true
                val matchingRequestProperty = requestPropertyMap[property.name.removePrefix("_")]
                val value = matchingRequestProperty?.getter?.call(request.data)

                if (value != null) {
                    property.setter.call(newInstance, value)
                }
            }

        if (newInstance is ExpandableBaseEntity && request.data is ExpandableBaseEntity) {
            val pseudoPropertiesFromRequest = objectMapper.readValue(
                request.data.pseudoProperties, object : TypeReference<Map<String, Any?>>() {}
            )

            if (pseudoPropertiesFromRequest.isNotEmpty()) {
                validatePseudoPropertiesFromRequest(newInstance, pseudoPropertiesFromRequest)
                newInstance.pseudoProperties = objectMapper.writeValueAsString(pseudoPropertiesFromRequest)
            }
        }

        val savedEntity = adapter.save(newInstance)
        val changes = EntityChangeTracker<T>().getChangedProperties(null, savedEntity)
        eventProducer.emit(entityClass.java, savedEntity.id, EntityEventType.CREATE, changes)

        return savedEntity
    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        val originalEntity = adapter.getById(request.id)
        if (originalEntity::class != entityClass) {
            throw IllegalArgumentException(
                "Entity type mismatch. Expected ${entityClass.simpleName} but found ${originalEntity::class.simpleName}."
            )
        }


        val requestStandardProperties = request.properties.filterKeys {
            it != "pseudoProperties" && !it.startsWith("_")
        }
        val updatedEntity = (originalEntity.copy() as T)
        val entityClassProperties = updatedEntity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        requestStandardProperties.forEach { (key, value) ->
            val property = entityClassProperties[key]
            if (property != null) {
                if (value == null && !property.returnType.isMarkedNullable) {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }
                if (value != null && value::class.createType() != property.returnType) {
                    throw IllegalArgumentException("Field $key must be of type ${property.returnType}")
                }
                property.setter.call(updatedEntity, value)
            } else throw IllegalArgumentException("Invalid property: $key")
        }

        if (updatedEntity is ExpandableBaseEntity) {
            val pseudoPropertiesFromRequest = request.properties["pseudoProperties"] as? Map<String, Any?>
                ?: emptyMap()

            if (pseudoPropertiesFromRequest.isNotEmpty()) {
                validatePseudoPropertiesFromRequest(updatedEntity, pseudoPropertiesFromRequest)

                val existingPseudoProperties: Map<String, Any?> = objectMapper.readValue(
                    updatedEntity.pseudoProperties, object : TypeReference<Map<String, Any?>>() {}
                )
                val mergedPseudoProperties = existingPseudoProperties + pseudoPropertiesFromRequest
                updatedEntity.pseudoProperties = objectMapper.writeValueAsString(mergedPseudoProperties)
            }
        }

        val savedEntity = adapter.save(updatedEntity)
        val changes = EntityChangeTracker<T>().getChangedProperties(originalEntity, updatedEntity)
        eventProducer.emit(updatedEntity::class.java, updatedEntity.id, EntityEventType.UPDATE, changes)

        return savedEntity
    }

    @Transactional
    override fun delete(id: UUID): HttpStatus {
        val entity = getSingle(id)
        adapter.delete(entity.id)
        eventProducer.emit(
            entity::class.java,
            id,
            EntityEventType.DELETE,
            mutableMapOf()
        ) //Fixme: should this be mutable?
        return HttpStatus.CREATED
    }

    override fun getSingle(id: UUID): T = adapter.getById(id)

    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun search(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)


    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: ExpandableBaseEntity, pseudoPropertiesFromRequest: Map<String, Any?>
    ) {
        val validPseudoProperties: Map<String, Any> = getValidPseudoProperties(updatedEntity)

        pseudoPropertiesFromRequest.forEach { (key, value) ->
            val expectedType = validPseudoProperties[key]
                ?: throw IllegalArgumentException("Invalid pseudo-property: $key")

            if (value != null && value::class.qualifiedName != expectedType::class.qualifiedName) {
                throw IllegalArgumentException("Pseudo-property $key must be of type $expectedType")
            }
        }
    }

    private fun getValidPseudoProperties(updatedEntity: ExpandableBaseEntity): Map<String, Any> {
        return pseudoPropertyRepository
            .findAllByEntitySimpleName(updatedEntity::class.simpleName!!)
            .associateBy { it.key }
            .mapValues {
                objectMapper.readValue(it.value.typeDescriptor, object : TypeReference<Any>() {})
            }

    }
}
