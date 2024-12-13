package com.ecommercedemo.common.application

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextProvider : ApplicationContextAware {
    companion object {
        lateinit var applicationContext: ApplicationContext
    }

    override fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }
}