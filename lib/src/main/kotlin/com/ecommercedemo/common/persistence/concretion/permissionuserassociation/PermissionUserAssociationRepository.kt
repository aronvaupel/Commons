package com.ecommercedemo.common.persistence.concretion.permissionuserassociation

import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import java.util.*

interface PermissionUserAssociationRepository : EntityRepository<PermissionUserAssociation, UUID>