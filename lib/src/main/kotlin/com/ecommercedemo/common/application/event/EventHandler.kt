package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST", "unused")
@Service
class EventHandler<T: BaseEntity>(
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

    private fun determineUseCaseForEvent(event: EntityEvent): IEventTypeUseCase<T> {
        val useCaseClass = when (event.type) {
            EntityEventType.CREATE -> ICreateTypeUseCase::class.java
            EntityEventType.UPDATE -> IUpdateTypeUseCase::class.java
            EntityEventType.DELETE -> IDeleteTypeUseCase::class.java
        } as Class<IEventTypeUseCase<T>>

        return getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClassName)
    }

    private fun <P : IEventTypeUseCase<T>> getEntitySpecificUseCaseByEventType(
        processorType: Class<P>,
        entityClassName: String
    ): P {
        val beans = applicationContext.getBeansOfType(processorType).values
        return beans.find { processor ->
            val firstTypeArgument = (processor::class.java.genericInterfaces.firstOrNull() as? ParameterizedType)
                ?.actualTypeArguments?.firstOrNull()
            println("Processor: ${processor::class.java.name}, FirstTypeArgument: $firstTypeArgument")
            firstTypeArgument == "_$entityClassName"::class.java
        } ?: throw IllegalArgumentException(
            "No processor of type ${processorType.simpleName} found for entity class: $entityClassName"
        )
    }
}





