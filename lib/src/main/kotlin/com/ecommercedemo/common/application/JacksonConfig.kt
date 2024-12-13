package com.ecommercedemo.common.application

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.EntityEventDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfig {

    @Bean
    open fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        val eventDeserializationModule = SimpleModule().apply {
            addDeserializer(EntityEvent::class.java, createEntityEventDeserializer(objectMapper))
        }

        return objectMapper.apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(eventDeserializationModule)
        }


    }

    private fun createEntityEventDeserializer(objectMapper: ObjectMapper): EntityEventDeserializer {
        val entityManager = SpringContextProvider.applicationContext.getBean(EntityManager::class.java)
        return EntityEventDeserializer(objectMapper, entityManager)
    }
}