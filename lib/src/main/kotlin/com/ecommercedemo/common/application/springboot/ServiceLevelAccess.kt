package com.ecommercedemo.common.application.springboot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "service-level-access")
data class ServiceLevelAccess(
    val restrictedTo: List<String> = emptyList()
)