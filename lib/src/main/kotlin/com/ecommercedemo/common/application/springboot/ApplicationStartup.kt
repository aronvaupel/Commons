package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.controller.annotation.AccessRestrictedToRoles
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.appinfo.EurekaInstanceConfig
import jakarta.annotation.PostConstruct
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val repositoryScanner: RepositoryScanner,
    private val applicationContext: ApplicationContext,
    private val serviceLevelAccess: ServiceLevelAccess,
    private val eurekaInstanceConfig: EurekaInstanceConfig
) {

    @PostConstruct
    fun init() {
        println("SERVICE LEVEL RESTRICTIONS: $serviceLevelAccess")
        val upstreamEntityNames = repositoryScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
        val enrichedEndpointMetadata = extractEndpointMetadata()
        registerRBACRulesToEureka(enrichedEndpointMetadata)
    }

    private fun extractEndpointMetadata(): List<EndpointMetadata> {
        val endpointMetadata = mutableListOf<EndpointMetadata>()
        val controllers = applicationContext.getBeansWithAnnotation(RestController::class.java)
            .values
            .map { AopProxyUtils.ultimateTargetClass(it) }
            .toSet()
            .filterNot { it.simpleName == "GatewayController" }
        println("Extracted Controllers: $controllers")
        controllers.forEach { controller ->
            val basePath = controller.getAnnotation(RequestMapping::class.java)?.value?.firstOrNull() ?: ""

            getAllMethods(controller).forEach { method ->
                val accessAnnotation: AccessRestrictedToRoles? =
                    method.getAnnotation(AccessRestrictedToRoles::class.java)

                method.annotations.forEach { annotation ->
                    if (annotation.annotationClass.simpleName?.endsWith("Mapping") == true) {

                        endpointMetadata.add(
                            EndpointMetadata(
                                path = combinePaths(basePath, extractMethodPath(annotation) ?: ""),
                                method = resolveHttpMethod(annotation),
                                roles = (accessAnnotation?.roles?.toSet()
                                    .orEmpty() + serviceLevelAccess.restrictedTo.toSet()),
                                pathVariables = extractPathVariables(method),
                                requestParameters = extractRequestParams(method)
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
        val serviceName = applicationContext.environment.getProperty("spring.application.name") ?: "unknown"
        val combined =
            ("/$serviceName" + basePath.trimEnd('/') + "/" + methodPath.trimStart('/')).replace("//", "/")
        println("Combined Paths: $combined")
        return combined
    }

    private fun extractMethodPath(annotation: Annotation): String? {
        return try {
            val value = annotation.annotationClass.java.getMethod("value").invoke(annotation)
            when (value) {
                is Array<*> -> value.firstOrNull()?.toString()
                else -> value?.toString()
            }.also { println("Extracted method path: $it") }
        } catch (e: Exception) {
            println("Failed to extract method path: ${e.message}")
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

    private fun getAllMethods(controller: Class<*>): List<Method> {
        val methods = mutableListOf<Method>()
        var currentClass: Class<*>? = controller
        while (currentClass != null) {
            methods.addAll(currentClass.declaredMethods)
            currentClass = currentClass.superclass
        }
        return methods
    }

    private fun resolveHttpMethod(annotation: Annotation): String {
        return when (annotation.annotationClass.simpleName) {
            "GetMapping" -> "GET"
            "PostMapping" -> "POST"
            "PutMapping" -> "PUT"
            "DeleteMapping" -> "DELETE"
            "PatchMapping" -> "PATCH"
            else -> throw IllegalArgumentException("Unsupported HTTP method annotation: $annotation")
        }
    }

    private fun extractPathVariables(method: Method): List<EndpointMethodParam> {
        return method.parameters
            .filter { it.isAnnotationPresent(PathVariable::class.java) }
            .mapIndexed { index, it ->
                EndpointMethodParam(
                    name = it.name,
                    position = index,
                    typeSimpleName = it.type.kotlin.simpleName!!
                )
            }
    }

    private fun extractRequestParams(method: Method): List<EndpointMethodParam> {
        return method.parameters
            .filter { it.isAnnotationPresent(RequestParam::class.java) }
            .mapIndexed { index, it ->
                EndpointMethodParam(
                    name = it.name,
                    position = index,
                    typeSimpleName = it.type.kotlin.simpleName!!
                )
            }
    }

}