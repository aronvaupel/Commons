package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.util.search.dto.SearchRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class PseudoPropertyAdapterPostgresql(
    private val pseudoPropertyRepository: PseudoPropertyRepository
) : IPseudoPropertyAdapter {

    override fun save(property: PseudoProperty): PseudoProperty {
        return pseudoPropertyRepository.save(property)
    }

    override fun getById(id: UUID): PseudoProperty {
        return pseudoPropertyRepository.findById(id).orElseThrow()
    }

    override fun getAll(request: SearchRequest): List<PseudoProperty> {
        return pseudoPropertyRepository.getAll(request)
    }

    override fun delete(id: UUID) {
        pseudoPropertyRepository.deleteById(id)
    }

}