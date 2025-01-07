package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface EntityRepository<T: BaseEntity, ID> : JpaRepository<T, ID>