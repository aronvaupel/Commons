package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import com.ecommercedemo.common.service.annotation.EventServiceFor
import com.ecommercedemo.common.service.annotation.RestServiceFor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.core.type.AnnotationMetadata

class TemplateBeanRegistrarTest {

    private val registry: BeanDefinitionRegistry = mock(BeanDefinitionRegistry::class.java)
    private val classPathScanner: ClassPathScanner = mock(ClassPathScanner::class.java)

    private lateinit var registrar: TemplateBeanRegistrar

    @BeforeEach
    fun setUp() {
        registrar = TemplateBeanRegistrar()
        `when`(classPathScanner.findClassesWithAnnotation(RestServiceFor::class))
            .thenReturn(setOf(RestServiceForExample::class.java))
        `when`(classPathScanner.findClassesWithAnnotation(EventServiceFor::class))
            .thenReturn(setOf(EventServiceForExample::class.java))
        `when`(classPathScanner.findClassesWithAnnotation(PersistenceAdapterFor::class))
            .thenReturn(setOf(PersistenceAdapterForExample::class.java))
        `when`(classPathScanner.findClassesWithAnnotation(ControllerFor::class))
            .thenReturn(setOf(ControllerForExample::class.java))
    }

    @Test
    fun `registerBeanDefinitions should register all annotated beans`() {
        registrar.registerBeanDefinitions(mock(AnnotationMetadata::class.java), registry)

        verify(registry).registerBeanDefinition(eq("restServiceForExample"), any(GenericBeanDefinition::class.java))
        verify(registry).registerBeanDefinition(eq("eventServiceForExample"), any(GenericBeanDefinition::class.java))
        verify(registry).registerBeanDefinition(
            eq("persistenceAdapterForExample"),
            any(GenericBeanDefinition::class.java)
        )
        verify(registry).registerBeanDefinition(eq("controllerForExample"), any(GenericBeanDefinition::class.java))
    }

    @Test
    fun `registerWithEntityClass should register beans with dependencies`() {
        registrar.registerWithEntityClass(RestServiceForExample::class.java, registry)

        verify(registry).registerBeanDefinition(eq("restServiceForExample"), any(GenericBeanDefinition::class.java))
    }

    @RestServiceFor(entity = ExampleEntity::class)
    class RestServiceForExample

    @EventServiceFor(entity = ExampleEntity::class)
    class EventServiceForExample

    @PersistenceAdapterFor(entity = ExampleEntity::class)
    class PersistenceAdapterForExample

    @ControllerFor(entity = ExampleEntity::class)
    class ControllerForExample

    class ExampleEntity
}
