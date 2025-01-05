package com.ecommercedemo.common.application.springboot

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(TemplateBeanRegistrar::class)
open class TemplateBeanRegistrarConfig