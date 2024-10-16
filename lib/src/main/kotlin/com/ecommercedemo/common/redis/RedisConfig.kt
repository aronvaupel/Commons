package com.ecommercedemo.common.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
open class RedisConfig {

    @Bean
    open fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory()
    }

    @Bean
    open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }
}
