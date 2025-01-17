package com.ecommercedemo.common.application.springboot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "access-restricted-to")
data class ServiceLevelAccess(
    val restrictedTo: List<String> = emptyList()
)