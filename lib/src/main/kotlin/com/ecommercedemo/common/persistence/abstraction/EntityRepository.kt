package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.jpa.repository.JpaRepository

@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
interface EntityRepository<T: BaseEntity, ID> : JpaRepository<T, ID>