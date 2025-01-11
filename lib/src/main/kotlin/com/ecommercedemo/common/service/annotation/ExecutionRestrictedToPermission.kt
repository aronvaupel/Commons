package com.ecommercedemo.common.service.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ExecutionRestrictedToPermission(val permission: String)