package com.ecommercedemo.common.service.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventServiceFor(val entity: KClass<*>)
