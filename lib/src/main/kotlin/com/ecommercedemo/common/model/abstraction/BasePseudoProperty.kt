package com.ecommercedemo.common.model.abstraction

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class BasePseudoProperty: BaseEntity() {
    open val entitySimpleName: String = ""
    open var key: String = ""
    open var typeDescriptor: String = ""
}