package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.controller.annotation.AccessRestrictedToRoles
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.appinfo.EurekaInstanceConfig
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.io.File

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val repositoryScanner: RepositoryScanner,
) {

    @Autowired
    private lateinit var eurekaInstanceConfig: EurekaInstanceConfig

    @Value("\${security.service-restriction.roles}")
    private lateinit var serviceLevelRestrictions: List<String>

    @PostConstruct
    fun init() {
        val upstreamEntityNames = repositoryScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
        val enrichedEndpointMetadata = extractEndpointRBACRules()
        registerRBACRulesToEureka(enrichedEndpointMetadata)
    }

    private fun extractEndpointRBACRules(): List<EndpointMetadata> {
        val endpointMetadata = mutableListOf<EndpointMetadata>()
        val controllerPackage = determineControllerPackage()
        val controllers = controllerPackage?.let {
            findClassesInPackage(controllerPackage).filter {
                it.isAnnotationPresent(RestController::class.java)
            }
        }

        controllers?.forEach { controller ->
            for (method in controller.declaredMethods) {
                val annotation = method.getAnnotation(AccessRestrictedToRoles::class.java)
                val requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping::class.java)

                requestMapping?.value?.forEach { path ->
                    requestMapping.method.forEach { httpMethod ->
                        endpointMetadata.add(
                            EndpointMetadata(
                                path = path,
                                method = httpMethod.name,
                                roles = (annotation?.roles?.toSet() ?: emptySet()) + serviceLevelRestrictions
                            )
                        )
                    }
                }
            }
        }

        println("Extracted Endpoint Metadata: $endpointMetadata")
        return endpointMetadata
    }

    private fun determineControllerPackage(): String? {
        try {
            val mainClass = Thread.currentThread().stackTrace.firstNotNullOfOrNull { stackElement ->
                val clazz = Class.forName(stackElement.className)
                if (clazz.isAnnotationPresent(SpringBootApplication::class.java)) clazz else null
            }
            val basePackage = Class.forName(mainClass?.name).`package`.name
            println("Base package: $basePackage")
            return "$basePackage.controller"
        } catch (e: ClassNotFoundException) {
            println("Could not determine base package.")
            return null
        }
    }


    private fun findClassesInPackage(basePackage: String): List<Class<*>> {
        val packagePath = basePackage.replace('.', '/')
        val classLoader = Thread.currentThread().contextClassLoader
        val resources = classLoader.getResources(packagePath)

        val classes = mutableListOf<Class<*>>()
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val files = File(resource.file).listFiles { _, name -> name.endsWith(".class") }
            files?.forEach { file ->
                val className = file.name.replace(".class", "")
                classes.add(Class.forName("$basePackage.$className"))
            }
        }
        println("Found following controller classes: $classes")
        return classes
    }

    private fun registerRBACRulesToEureka(
        enrichedEndpointMetadata: List<EndpointMetadata>
    ) {
        val metadata = mutableMapOf<String, String>()
        metadata["endpoints"] = ObjectMapper().writeValueAsString(enrichedEndpointMetadata)
        println("Registered Enriched Metadata to Eureka: $metadata")
        eurekaInstanceConfig.metadataMap.putAll(metadata)
    }
}