package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST", "unused")
@Service
class EventHandler(
    private val applicationContext: ApplicationContext
) {

    fun <T : BaseEntity> handle(event: EntityEvent<T>) {
        println("Received event for ${event.entityClass.simpleName} with ID: ${event.id}")
        try {
           determineUseCaseForEvent(event).applyChanges(event)
        } catch (e: Exception) {
            println(
                "Error processing event for ${event.entityClass.simpleName} with ID: ${event.id}. Error: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    private fun <T : BaseEntity> determineUseCaseForEvent(event: EntityEvent<T>): IEventTypeUseCase<T> {
        println("Determining use case for event: $event")
        val useCaseClass = when (event.type) {
            EntityEventType.CREATE -> {
                println("Event type is CREATE")
                ICreateTypeUseCase::class.java
            }

            EntityEventType.UPDATE -> IUpdateTypeUseCase::class.java
            EntityEventType.DELETE -> IDeleteTypeUseCase::class.java
        } as Class<IEventTypeUseCase<T>>

        return getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClass)
    }

    private fun <T : BaseEntity, P : IEventTypeUseCase<T>> getEntitySpecificUseCaseByEventType(
        processorType: Class<P>,
        entityClass: Class<T>
    ): P {
        println("Attempt to find matching use case")
        val beans = applicationContext.getBeansOfType(processorType).values
        println("Found following beans: $beans")
        return beans.find { processor ->
            (processor::class.java.genericInterfaces.firstOrNull() as? ParameterizedType)
                ?.actualTypeArguments?.firstOrNull() == entityClass
        } ?: throw IllegalArgumentException(
            "No processor of type ${processorType.simpleName} found for entity class: ${entityClass.simpleName}"
        )
    }
}




