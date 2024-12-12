package com.ecommercedemo.common.model.concretion.pseudoProperty

import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import jakarta.persistence.Entity
import jakarta.persistence.Table

//Todo: Expiration
@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty: BasePseudoProperty() {
    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }
}
