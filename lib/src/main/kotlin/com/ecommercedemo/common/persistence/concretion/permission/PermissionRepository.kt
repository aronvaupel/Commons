package com.ecommercedemo.common.persistence.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.util.*

@ConditionalOnProperty(name = ["permissions"], havingValue = "true", matchIfMissing = false)
interface PermissionRepository : EntityRepository<Permission, UUID>