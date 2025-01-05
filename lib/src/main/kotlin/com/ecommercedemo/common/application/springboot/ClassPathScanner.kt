package com.ecommercedemo.common.application.springboot

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ClassPathScanner {

    fun <A : Annotation> findClassesWithAnnotation(annotation: KClass<A>): Set<Class<*>> {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(annotation.java))

        val classLoader = Thread.currentThread().contextClassLoader
        val classNames = scanner.findCandidateComponents("com").map { it.beanClassName }

        return classNames.mapNotNull { className ->
            try {
                Class.forName(className, true, classLoader)
            } catch (e: ClassNotFoundException) {
                null
            }
        }.toSet()
    }
}