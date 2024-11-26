package com.ecommercedemo.common.model.abstraction

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class BasePseudoProperty: BaseEntity() {
    abstract val entitySimpleName: String
    abstract var key: String
    abstract var typeDescriptor: String
}