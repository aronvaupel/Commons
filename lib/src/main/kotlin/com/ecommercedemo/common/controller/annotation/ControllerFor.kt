package com.ecommercedemo.common.controller.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ControllerFor(val entity: KClass<*>)
