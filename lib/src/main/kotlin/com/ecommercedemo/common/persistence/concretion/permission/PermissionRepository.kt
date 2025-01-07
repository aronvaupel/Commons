package com.ecommercedemo.common.persistence.concretion.permission

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import java.util.*

interface PermissionRepository : EntityRepository<Permission, UUID>