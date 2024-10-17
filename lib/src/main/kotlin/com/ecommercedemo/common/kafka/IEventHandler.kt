package com.ecommercedemo.common.kafka

interface IEventHandler<T> {
    fun handle(event:T)
}