package com.ecommercedemo.common.persistence

interface IPseudoPropertyManagement {
    fun updatePseudoPropertyForAllEntities(key: String, value: Any?)
    fun deletePseudoPropertyForAllEntities(key: String)
}