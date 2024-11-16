package com.ecommercedemo.common.application.event

interface IEventHandler<T> {
    fun handle(event:T)
}