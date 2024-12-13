package com.ecommercedemo.common.application

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.EntityEventDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfig {

    @Bean
    open fun customObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()

        val module = SimpleModule().apply {
            addDeserializer(EntityEvent::class.java, createEntityEventDeserializer())
        }
        objectMapper.registerModule(module)

        return objectMapper
    }

    private fun createEntityEventDeserializer(): EntityEventDeserializer {
        val entityManager = SpringContextProvider.applicationContext.getBean(EntityManager::class.java)
        val objectMapper = SpringContextProvider.applicationContext.getBean(ObjectMapper::class.java)
        return EntityEventDeserializer(objectMapper, entityManager)
    }
}