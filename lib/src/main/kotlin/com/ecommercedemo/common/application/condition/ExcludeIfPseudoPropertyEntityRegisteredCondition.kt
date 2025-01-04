package com.ecommercedemo.common.application.condition

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ExcludeIfPseudoPropertyEntityRegisteredCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val entityManagerFactoryBean = context.beanFactory?.getBean(EntityManagerFactory::class.java)
        return if (entityManagerFactoryBean != null) {
            !entityManagerFactoryBean.metamodel.entities.any { it.name == "PseudoProperty" }
        } else {
            true
        }
    }
}