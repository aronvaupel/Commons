package com.ecommercedemo.common.model.concretion._pseudoProperty

import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Suppress("unused", "ClassName")
@Entity
@Table(name = _PseudoProperty.STORAGE_NAME)
open class _PseudoProperty: BasePseudoProperty() {
    companion object {
        const val STORAGE_NAME = "_pseudo_properties"
    }
}