package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.application.search.Retriever
import com.ecommercedemo.common.application.search.dto.SearchRequest
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import org.springframework.stereotype.Service
import java.util.*

@Service
class PseudoPropertyAdapterPostgresql(
    private val pseudoPropertyRepository: PseudoPropertyRepository,
    private val retriever: Retriever
) : IPseudoPropertyAdapter {

    override fun save(dto: PseudoPropertyDto): PseudoProperty {
        println("Creating pseudo property from dto: $dto")
        val property = PseudoProperty(
            id = UUID.randomUUID(),
            entitySimpleName = dto.entityClassName,
            key = dto.key,
            typeDescriptor = dto.typeDescriptor
        )
        println("Instantiated pseudo property: $property")
        return  pseudoPropertyRepository.save(property)

    }

    override fun getById(id: UUID): PseudoProperty {
        return pseudoPropertyRepository.findById(id).orElseThrow()
    }

    override fun getPseudoProperties(request: SearchRequest): List<PseudoProperty> {
        return retriever.executeSearch(request, PseudoProperty::class)
    }

    override fun delete(id: UUID) {
        pseudoPropertyRepository.deleteById(id)
    }

}