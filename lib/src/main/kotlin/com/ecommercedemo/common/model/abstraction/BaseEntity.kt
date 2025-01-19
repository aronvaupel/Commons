package com.ecommercedemo.common.model.abstraction

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible


@MappedSuperclass
@Suppress("unused", "JpaQlInspection")
@Schema(description = "Base class for all entities with common fields.")
abstract class BaseEntity{
    @Schema(description = "Unique identifier for the entity.", example = "123e4567-e89b-12d3-a456-426614174000")
    @Id
    open var id: UUID = UUID.randomUUID()

    @Schema(description = "Timestamp when the entity was created.", example = "2023-01-01T10:00:00Z")
    @Column(nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    open var createdAt: LocalDateTime = LocalDateTime.now()

    @Schema(description = "Timestamp when the entity was last updated.", example = "2023-01-02T15:00:00Z")
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
            val property = this::class.memberProperties.firstOrNull { it.name == param.name }
                ?: throw IllegalArgumentException("No property found for parameter '${param.name}'")

            try {
                property.getter.apply { isAccessible = true }.call(this)
            } catch (e: IllegalAccessException) {
                val publicGetter = this::class.memberProperties.firstOrNull { it.name == param.name?.removePrefix("_") }
                publicGetter?.getter?.call(this)
            }
        }

        val instance = constructor.callBy(args)

        this::class.memberProperties
            .filter { property -> constructor.parameters.none { it.name == property.name } }
            .forEach { property ->
                try {
                    val value = when {
                        property.name.startsWith("_") -> {
                            val publicGetterName = property.name.removePrefix("_")
                            this::class.memberProperties
                                .firstOrNull { it.name == publicGetterName }
                                ?.getter
                                ?.call(this)
                        }
                        else -> property.getter.call(this)
                    }
                    if (property is KMutableProperty<*>) {
                        property.setter.call(instance, value)
                    }
                } catch (e: Exception) {
                    println("WARNING: Failed to copy property '${property.name}' due to: ${e.message}")
                }
            }

        return instance
    }

}
