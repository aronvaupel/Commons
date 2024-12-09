package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IDeleteTypeUseCase<T: BaseEntity> : IEventTypeUseCase<T>