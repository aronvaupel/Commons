package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.controller.annotation.AccessRestrictedToRoles
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.appinfo.EurekaInstanceConfig
import jakarta.annotation.PostConstruct
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val repositoryScanner: RepositoryScanner,
    private val applicationContext: ApplicationContext
) {

    @Autowired
    private lateinit var eurekaInstanceConfig: EurekaInstanceConfig

    @Value("\${security.service-restriction.roles:[]}")
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
        val controllers = applicationContext.getBeansWithAnnotation(RestController::class.java)
            .values
            .map { AopProxyUtils.ultimateTargetClass(it) }
            .toSet()
            .filterNot { it.simpleName == "GatewayController" }
        println("Extracted Controllers: $controllers")
        controllers.forEach { controller ->
            val classLevelRequestMapping = controller.getAnnotation(RequestMapping::class.java)
            val basePath = classLevelRequestMapping?.value?.firstOrNull() ?: ""

            controller.declaredMethods.forEach { method ->
                val accessAnnotation: AccessRestrictedToRoles? =
                    method.getAnnotation(AccessRestrictedToRoles::class.java) ?: null

                method.annotations.forEach { annotation ->
                    if (annotation.annotationClass.simpleName?.endsWith("Mapping") == true) {
                        val methodPath = extractAnnotationValue(annotation, "value") ?: ""
                        val fullPath = combinePaths(basePath, methodPath)

                        val httpMethod = extractAnnotationValue(annotation, "method") ?: ""

                        val roles = (accessAnnotation?.roles?.toSet() ?: emptySet()) + serviceLevelRestrictions

                        endpointMetadata.add(
                            EndpointMetadata(
                                path = fullPath,
                                method = httpMethod,
                                roles = roles
                            )
                        )
                    }
                }
            }
        }

        println("Extracted Endpoint Metadata: $endpointMetadata")
        return endpointMetadata
    }


    private fun combinePaths(basePath: String, methodPath: String): String {
        val combined =
            (basePath.trimEnd('/') + "/" + methodPath.trimStart('/')).replace("//", "/")
        println("Combined Paths: $combined")
        return combined
    }

    private fun extractAnnotationValue(annotation: Annotation, fieldName: String): String? {
        return try {
            val method = annotation.annotationClass.java.getMethod(fieldName)
            val value = method.invoke(annotation)
            when (value) {
                is Array<*> -> value.first()?.toString()
                else -> value?.toString()
            }.also { println("Extracted annotation value: $it") }
        } catch (e: Exception) {
            println("Extraction of annotation value failed: ${e.message}")
            null
        }
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