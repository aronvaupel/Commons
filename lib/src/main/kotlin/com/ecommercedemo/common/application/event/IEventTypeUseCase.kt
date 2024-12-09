package com.ecommercedemo.common.application.event

interface IEventTypeUseCase {
    fun applyChanges(event: EntityEvent) {
    }
}