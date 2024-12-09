package com.ecommercedemo.common.application.event

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST", "unused")
@Service
class EventHandler(
    private val applicationContext: ApplicationContext
) {

    fun handle(event: EntityEvent) {
        try {
           determineUseCaseForEvent(event).applyChanges(event)
        } catch (e: Exception) {
            println(
                "Error processing event for ${event.entityClassName} with ID: ${event.id}. Error: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    private fun determineUseCaseForEvent(event: EntityEvent): IEventTypeUseCase {
        println("Determining use case for event: $event")
        val useCaseClass = when (event.type) {
            EntityEventType.CREATE -> {
                println("Event type is CREATE")
                ICreateTypeUseCase::class.java
            }

            EntityEventType.UPDATE -> IUpdateTypeUseCase::class.java
            EntityEventType.DELETE -> IDeleteTypeUseCase::class.java
        } as Class<IEventTypeUseCase>

        return getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClassName)
    }

    private fun <T : IEventTypeUseCase> getEntitySpecificUseCaseByEventType(
        processorType: Class<T>,
        entityClassName: String
    ): T {
        val beans = applicationContext.getBeansOfType(processorType).values
        return beans.find { processor ->
            (processor::class.java.genericInterfaces.firstOrNull() as? ParameterizedType)
                ?.actualTypeArguments?.firstOrNull() == entityClassName::class.java
        } ?: throw IllegalArgumentException(
            "No processor of type ${processorType.simpleName} found for entity class: $entityClassName"
        )
    }
}





