package com.ecommercedemo.common.application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfig {
    @Bean
    open fun customizeObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
        objectMapper.registerModule(JavaTimeModule())
        return objectMapper
    }
}
