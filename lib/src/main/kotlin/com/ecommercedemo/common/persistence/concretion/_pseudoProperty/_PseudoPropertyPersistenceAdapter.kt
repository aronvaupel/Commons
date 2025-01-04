package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyEntityRegisteredCondition
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@PersistenceAdapterFor(_PseudoProperty::class)
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
@Conditional(ExcludeIfPseudoPropertyEntityRegisteredCondition::class)
class _PseudoPropertyPersistenceAdapter : EntityPersistenceAdapter<_PseudoProperty>()