package com.ecommercedemo.common.persistence.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import org.springframework.stereotype.Service

@Service
@PersistenceAdapterFor(Permission::class)
class PermissionPersistenceAdapter : EntityPersistenceAdapter<Permission>()