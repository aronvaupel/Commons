package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.util.search.dto.SearchRequest
import java.util.*


interface IPseudoPropertyAdapter {
    fun save(property: PseudoProperty): PseudoProperty
    fun delete(id: UUID)
    fun getById(id: UUID): PseudoProperty
    fun getAll(request: SearchRequest): List<PseudoProperty>
}