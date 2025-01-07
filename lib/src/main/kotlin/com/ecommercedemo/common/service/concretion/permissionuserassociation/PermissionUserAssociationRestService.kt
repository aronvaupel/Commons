package com.ecommercedemo.common.service.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@RestServiceFor(PermissionUserAssociation::class)
@Transactional
@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
open class PermissionUserAssociationRestService : RestServiceTemplate<PermissionUserAssociation>()
