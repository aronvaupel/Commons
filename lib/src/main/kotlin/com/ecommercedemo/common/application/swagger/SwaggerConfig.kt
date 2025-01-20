package com.ecommercedemo.common.application.swagger

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SwaggerConfig {
    @Value("\${spring.application.name}")
    private val applicationName: String? = null

    @Bean
    open fun defaultApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group(applicationName)
            .pathsToMatch("/**")
            .build()
    }
}