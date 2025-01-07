package com.ecommercedemo.common.service.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.stereotype.Service

@Service
@EventServiceFor(PermissionUserAssociation::class)
open class PermissionUserAssociationEventService : EventServiceTemplate<PermissionUserAssociation>()