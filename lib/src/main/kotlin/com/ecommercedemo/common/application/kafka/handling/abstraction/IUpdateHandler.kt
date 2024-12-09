package com.ecommercedemo.common.application.kafka.handling.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IUpdateHandler<T: BaseEntity> : IEventTypeHandler<T>
