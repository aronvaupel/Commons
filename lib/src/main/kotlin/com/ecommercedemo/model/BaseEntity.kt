package com.ecommercedemo.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID


@MappedSuperclass
@Suppress("unused")
abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}