package com.ecommercedemo.common.model.abstraction

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


@MappedSuperclass
@Suppress("unused", "JpaQlInspection")
abstract class BaseEntity{
    @Id
    @GeneratedValue(generator = "UUID")
    open val id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)

    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    open var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    open var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun copy(): BaseEntity {
        val constructor = this::class.primaryConstructor
            ?: throw IllegalStateException("No primary constructor for ${this::class.simpleName}")

        val args = constructor.parameters.associateWith { param ->
            this::class.memberProperties.firstOrNull { it.name == param.name }?.getter?.call(this)
        }

        val instance = constructor.callBy(args)

        this::class.memberProperties
            .filter { property ->
                constructor.parameters.none { it.name == property.name }
            }
            .forEach { property ->
                try {
                    val value = property.getter.call(this)
                    if (property is KMutableProperty<*>) {
                        println("DEBUG: Copying mutable property '${property.name}' with value: $value")
                        property.setter.call(instance, value)
                    } else {
                        println("DEBUG: Skipping immutable property '${property.name}' with value: $value")
                    }
                } catch (e: Exception) {
                    println("WARNING: Failed to copy property '${property.name}' due to: ${e.message}")
                }
            }

        return instance
    }


}
