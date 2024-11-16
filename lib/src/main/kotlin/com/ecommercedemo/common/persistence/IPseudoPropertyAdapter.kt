package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.application.search.dto.SearchRequest
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import java.util.*


interface IPseudoPropertyAdapter {
    fun save(property: PseudoPropertyDto): PseudoProperty
    fun delete(id: UUID)
    fun getById(id: UUID): PseudoProperty
    fun getPseudoProperties(request: SearchRequest): List<PseudoProperty>
}