package com.ecommercedemo.common.service

import com.ecommercedemo.common.application.ClassPathScanner
import com.ecommercedemo.common.controller.ControllerFor
import com.ecommercedemo.common.persistence.PersistenceAdapterFor
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

class TemplateBeanRegistrar : ImportBeanDefinitionRegistrar {
   // private val applicationContext = SpringContextProvider.applicationContext

    override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val scanner = ClassPathScanner()

        scanner.findClassesWithAnnotation(RestServiceFor::class).forEach { clazz ->
            val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).beanDefinition
            registry.registerBeanDefinition(clazz.simpleName, beanDefinition)
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

//    private fun registerBean(clazz: Class<*>, registry: BeanDefinitionRegistry, context: ApplicationContext) {
//        val kClass = clazz.kotlin
//
//        val annotation = kClass.findAnnotation<RestServiceFor>()
//            ?: throw IllegalStateException("No @RestServiceFor annotation found on ${clazz.simpleName}")
//
//        val entityClass = annotation.entity.java.kotlin
//
//        val parentClass = kClass.superclasses.firstOrNull()
//            ?: throw IllegalStateException("No superclass found for ${clazz.simpleName}")
//
//        val parentConstructor = parentClass.primaryConstructor
//            ?: throw IllegalStateException("No primary constructor found for ${parentClass.simpleName}")
//
//        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz)
//
//        parentConstructor.parameters.forEach { param ->
//            val paramClass = param.type.classifier?.javaClass
//            val dependency = if (param.name == "entityClass") {
//                entityClass
//            } else {
//                paramClass?.let { context.getBean(it) }
//                    ?: throw IllegalStateException("No bean found for parameter ${param.name}")
//            }
//            beanDefinitionBuilder.addConstructorArgValue(dependency)
//        }
//
//        registry.registerBeanDefinition(clazz.simpleName, beanDefinitionBuilder.beanDefinition)
//    }

}
