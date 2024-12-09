package com.ecommercedemo.common.application.kafka.handling.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IHandleHandleUpdate<T: BaseEntity> : IHandleEventType<T>
