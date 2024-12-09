package com.ecommercedemo.common.application.kafka.handling.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IHandleCreate<T: BaseEntity> : IHandleEventType<T>
