package com.ecommercedemo.common.security

import io.github.cdimascio.dotenv.Dotenv
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class EnvConfig {
    @Value("\${app.env.local:false}")
    var isLocalEnv: Boolean = false

    @PostConstruct
    fun init() {
        if (isLocalEnv) {
            Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load()
        }
    }
}