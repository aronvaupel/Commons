package com.ecommercedemo.common.application

import com.ecommercedemo.common.service.TemplateBeanRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(TemplateBeanRegistrar::class)
open class TemplateBeanRegistrarConfig