package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BasePseudoProperty

interface IPseudoPropertyRepository <T: BasePseudoProperty> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<T>
}