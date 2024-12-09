package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface ICreateTypeUseCase<T: BaseEntity> : IEventTypeUseCase<T>
