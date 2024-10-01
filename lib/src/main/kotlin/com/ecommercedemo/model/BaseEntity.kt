package com.ecommercedemo.model

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime
import java.util.UUID


@MappedSuperclass
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