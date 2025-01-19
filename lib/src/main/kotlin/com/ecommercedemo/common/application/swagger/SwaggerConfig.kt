package com.ecommercedemo.common.application.swagger

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SwaggerConfig {

    @Bean
    open fun defaultApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("default")
            .pathsToMatch("/**")
            .build()
    }
}