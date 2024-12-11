package com.ecommercedemo.common.application.kafka.handling

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.EntityEventType
import com.ecommercedemo.common.application.kafka.handling.abstraction.ICreateHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IDeleteHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IEventTypeHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IUpdateHandler
import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST", "unused")
@Service
class MainEventHandler<T: BaseEntity>(
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

    private fun determineUseCaseForEvent(event: EntityEvent): IEventTypeHandler<T> {
        val useCaseClass = when (event.type) {
            EntityEventType.CREATE -> ICreateHandler::class.java
            EntityEventType.UPDATE -> IUpdateHandler::class.java
            EntityEventType.DELETE -> IDeleteHandler::class.java
        } as Class<IEventTypeHandler<T>>
        println("Event type: ${event.type}")
        val result = getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClassName)
        println("Use case: $result")
        return result
    }

    private fun <P : IEventTypeHandler<T>> getEntitySpecificUseCaseByEventType(
        processorType: Class<P>,
        entityClassName: String
    ): P {
        val beans = applicationContext.getBeansOfType(processorType).values
        return beans.find { processor ->
            val firstTypeArgument = (processor::class.java.genericInterfaces.firstOrNull() as? ParameterizedType)
                ?.actualTypeArguments?.firstOrNull()
            firstTypeArgument?.typeName?.substringAfterLast(".") == "_$entityClassName"
        } ?: throw IllegalArgumentException(
            "No processor of type ${processorType.simpleName} found for entity class: $entityClassName"
        )
    }
}





