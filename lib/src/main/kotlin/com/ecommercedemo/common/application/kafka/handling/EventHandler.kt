package com.ecommercedemo.common.application.kafka.handling

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.EntityEventType
import com.ecommercedemo.common.application.kafka.handling.abstraction.IHandleCreate
import com.ecommercedemo.common.application.kafka.handling.abstraction.IHandleEventType
import com.ecommercedemo.common.application.kafka.handling.abstraction.IHandleHandleDelete
import com.ecommercedemo.common.application.kafka.handling.abstraction.IHandleHandleUpdate
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

    private fun determineUseCaseForEvent(event: EntityEvent): IHandleEventType<T> {
        val useCaseClass = when (event.type) {
            EntityEventType.CREATE -> IHandleCreate::class.java
            EntityEventType.UPDATE -> IHandleHandleUpdate::class.java
            EntityEventType.DELETE -> IHandleHandleDelete::class.java
        } as Class<IHandleEventType<T>>
        println("Event type: ${event.type}")
        val result = getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClassName)
        println("Use case: $result")
        return result
    }

    private fun <P : IHandleEventType<T>> getEntitySpecificUseCaseByEventType(
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





