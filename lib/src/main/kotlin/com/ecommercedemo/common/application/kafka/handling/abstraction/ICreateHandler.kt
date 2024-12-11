package com.ecommercedemo.common.application.kafka.handling.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface ICreateHandler<T: BaseEntity> : IEventTypeHandler<T>
