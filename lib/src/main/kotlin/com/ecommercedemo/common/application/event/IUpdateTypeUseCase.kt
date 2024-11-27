package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IUpdateTypeUseCase<T: BaseEntity> : IEventTypeUseCase<T>
