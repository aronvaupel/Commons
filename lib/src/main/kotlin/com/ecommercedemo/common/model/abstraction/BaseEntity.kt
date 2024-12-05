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
import kotlin.reflect.jvm.isAccessible


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
                    val value = if (property.name.startsWith("_")) {
                        val publicGetterName = property.name.removePrefix("_")
                        this::class.memberProperties
                            .firstOrNull { it.name == publicGetterName }
                            ?.apply { isAccessible = true }
                            ?.getter
                            ?.call(this)
                            ?: run {
                                println("WARNING: No public getter for private property '${property.name}'")
                                null
                            }
                    } else {
                        property.getter.call(this)
                    }
                    if (value != null) {
                        val setterName = if (property.name.startsWith("_")) {
                            // Match the public setter for private fields
                            property.name.removePrefix("_")
                        } else {
                            property.name
                        }
                        val matchingSetter = this::class.memberProperties
                            .firstOrNull { it.name == setterName && it is KMutableProperty<*> }
                                as? KMutableProperty<*>

                        if (matchingSetter != null) {
                            matchingSetter.setter.call(instance, value)
                        } else if (property is KMutableProperty<*>) {
                            // If no matching setter is found, fallback to using the property setter
                            property.setter.call(instance, value)
                        } else {
                            println("WARNING: No matching setter for '${property.name}'. Skipping.")
                        }
                    }
                } catch (e: Exception) {
                    println("ERROR: Failed to copy property '${property.name}': ${e.message}")
                }
            }

        return instance
    }

}
