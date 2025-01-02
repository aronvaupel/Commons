package com.ecommercedemo.common.application.jackson

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.EntityEventDeserializer
import com.ecommercedemo.common.application.springboot.SpringContextProvider
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@DependsOn("springContextProvider")
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
        return EntityEventDeserializer(
            objectMapper,
            SpringContextProvider.applicationContext.getBean(TypeReAttacher::class.java)
        )
    }
}