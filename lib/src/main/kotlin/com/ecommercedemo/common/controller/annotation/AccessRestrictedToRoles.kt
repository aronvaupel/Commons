package com.ecommercedemo.common.controller.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AccessRestrictedToRoles(val roles: Array<String>)