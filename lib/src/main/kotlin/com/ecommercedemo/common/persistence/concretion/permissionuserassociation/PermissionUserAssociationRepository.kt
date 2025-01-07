package com.ecommercedemo.common.persistence.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.util.*

@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
interface PermissionUserAssociationRepository : EntityRepository<PermissionUserAssociation, UUID>