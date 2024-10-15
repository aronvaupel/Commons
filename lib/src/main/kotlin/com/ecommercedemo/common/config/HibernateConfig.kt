package com.ecommercedemo.common.config

import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import org.hibernate.boot.model.TypeContributions
import org.hibernate.boot.model.TypeContributor
import org.hibernate.service.ServiceRegistry
import org.springframework.context.annotation.Configuration

@Configuration
open class HibernateConfig : TypeContributor {
    override fun contribute(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry) {
        typeContributions.contributeType(UUIDArrayType())
    }
}