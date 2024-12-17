package com.ecommercedemo.common.service

import com.ecommercedemo.common.application.ClassPathScanner
import com.ecommercedemo.common.application.SpringContextProvider
import com.ecommercedemo.common.controller.ControllerFor
import com.ecommercedemo.common.persistence.PersistenceAdapterFor
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

class TemplateBeanRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val scanner = ClassPathScanner()

        scanner.findClassesWithAnnotation(RestServiceFor::class).forEach { clazz ->
            val entityClass = clazz.getAnnotation(RestServiceFor::class.java).entity.java
            val constructor = clazz.constructors.firstOrNull()
                ?: throw IllegalStateException("No constructor found for parent class of ${clazz.simpleName}")

            val dependencies = constructor.parameterTypes.map { paramType ->
                when (paramType) {
                    entityClass::class.java -> entityClass
                    else -> SpringContextProvider.applicationContext.getBean(paramType)
                }
            }

            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz)
            dependencies.forEach { dep -> beanDefinition.addConstructorArgValue(dep) }
            registry.registerBeanDefinition(clazz.simpleName, beanDefinition.beanDefinition)
        }

        scanner.findClassesWithAnnotation(EventServiceFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(clazz.simpleName, beanDefinition)
        }
        scanner.findClassesWithAnnotation(PersistenceAdapterFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(clazz.simpleName, beanDefinition)
        }
        scanner.findClassesWithAnnotation(ControllerFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(clazz.simpleName, beanDefinition)
        }
    }

}
