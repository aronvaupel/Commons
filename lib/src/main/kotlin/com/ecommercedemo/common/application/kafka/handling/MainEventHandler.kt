package com.ecommercedemo.common.application.kafka.handling

import com.ecommercedemo.common.application.exception.FailedToHandleEventException
import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.handling.abstraction.ICreateHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IDeleteHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IEventTypeHandler
import com.ecommercedemo.common.application.kafka.handling.abstraction.IUpdateHandler
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.model.abstraction.BaseEntity
import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST", "unused")
@Service
class MainEventHandler<T: BaseEntity>(
    private val applicationContext: ApplicationContext
) {
    private val log = KotlinLogging.logger {}

    fun handle(event: EntityEvent) {
        try {
           determineUseCaseForEvent(event).applyChanges(event)
        } catch (e: Exception) {
            log.warn { "Failed to handle event. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToHandleEventException("Failed to handle event: $event", e)
        }
    }

    private fun determineUseCaseForEvent(event: EntityEvent): IEventTypeHandler<T> {
        val useCaseClass = when (event.type) {
            ModificationType.CREATE -> ICreateHandler::class.java
            ModificationType.UPDATE -> IUpdateHandler::class.java
            ModificationType.DELETE -> IDeleteHandler::class.java
        } as Class<IEventTypeHandler<T>>
        val result = getEntitySpecificUseCaseByEventType(useCaseClass, event.entityClassName)
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





