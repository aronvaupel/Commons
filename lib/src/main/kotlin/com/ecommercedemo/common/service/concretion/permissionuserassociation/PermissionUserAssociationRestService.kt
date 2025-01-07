package com.ecommercedemo.common.service.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@RestServiceFor(PermissionUserAssociation::class)
@Transactional
open class PermissionUserAssociationRestService : RestServiceTemplate<PermissionUserAssociation>()
