package com.ecommercedemo.common.application.springboot

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(TemplateBeanRegistrar::class)
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
open class TemplateBeanRegistrarConfig