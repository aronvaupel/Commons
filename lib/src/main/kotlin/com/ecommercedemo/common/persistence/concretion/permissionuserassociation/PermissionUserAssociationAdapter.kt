package com.ecommercedemo.common.persistence.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@PersistenceAdapterFor(PermissionUserAssociation::class)
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
class PermissionUserAssociationAdapter : EntityPersistenceAdapter<PermissionUserAssociation>()