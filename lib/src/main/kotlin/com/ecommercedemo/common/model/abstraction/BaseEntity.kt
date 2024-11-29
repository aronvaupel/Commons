package com.ecommercedemo.common.model.abstraction

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*


@MappedSuperclass
@Suppress("unused", "JpaQlInspection")
abstract class BaseEntity{
    @Id
    @GeneratedValue(generator = "UUID")
    open val id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    open var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
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

    abstract fun copy(): BaseEntity
}