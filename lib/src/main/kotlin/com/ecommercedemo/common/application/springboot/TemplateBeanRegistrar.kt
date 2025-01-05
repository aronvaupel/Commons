package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import com.ecommercedemo.common.service.annotation.EventServiceFor
import com.ecommercedemo.common.service.annotation.RestServiceFor
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import java.util.*

class TemplateBeanRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val scanner = ClassPathScanner()

        scanner.findClassesWithAnnotation(RestServiceFor::class).forEach { clazz ->
            registerWithEntityClass(clazz, registry)
        }
        scanner.findClassesWithAnnotation(EventServiceFor::class).forEach { clazz ->
            registerWithEntityClass(clazz, registry)
        }
        scanner.findClassesWithAnnotation(PersistenceAdapterFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(
                clazz.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                beanDefinition
            )
        }
        scanner.findClassesWithAnnotation(ControllerFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(
                clazz.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                beanDefinition
            )
        }
    }

    private fun registerWithEntityClass(
        clazz: Class<*>,
        registry: BeanDefinitionRegistry,
    ) {
        val entityClass = when {
            clazz.isAnnotationPresent(RestServiceFor::class.java) -> {
                clazz.getAnnotation(RestServiceFor::class.java).entity
            }

            clazz.isAnnotationPresent(EventServiceFor::class.java) -> {
                clazz.getAnnotation(EventServiceFor::class.java).entity
            }

            else -> throw IllegalStateException("No valid annotation found on class ${clazz.name}")
        }

        val parentConstructor = clazz.superclass.constructors.firstOrNull()
            ?: throw IllegalStateException("No constructor found for parent class of ${clazz.simpleName}")

        val dependencies = parentConstructor.parameterTypes.map { paramType ->
            when (paramType) {
                entityClass::class.java -> entityClass
                else -> SpringContextProvider.applicationContext.getBean(paramType)
            }
        }

        val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz)
        dependencies.forEach { dep -> beanDefinition.addConstructorArgValue(dep) }
        registry.registerBeanDefinition(
            clazz.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) },
            beanDefinition.beanDefinition
        )
    }

}
