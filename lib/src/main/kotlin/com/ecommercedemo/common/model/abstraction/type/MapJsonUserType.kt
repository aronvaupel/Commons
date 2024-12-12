package com.ecommercedemo.common.model.abstraction.type

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class MapJsonUserType : UserType<Map<String, Any>> {

    private val objectMapper = ObjectMapper()

    fun sqlTypes(): IntArray {
        return intArrayOf(Types.OTHER)
    }

    override fun getSqlType(): Int {
        return Types.OTHER
    }

    @Suppress("UNCHECKED_CAST")
    override fun returnedClass(): Class<Map<String, Any>> {
        return Map::class.java as Class<Map<String, Any>>
    }

    override fun equals(p0: Map<String, Any>?, p1: Map<String, Any>?): Boolean {
        return p0 == p1
    }

    override fun hashCode(p0: Map<String, Any>?): Int {
        return p0?.hashCode() ?: 0
    }

    override fun nullSafeGet(
        rs: ResultSet?,
        p1: Int,
        session: SharedSessionContractImplementor?,
        owner: Any?
    ): Map<String, Any>? {
        val json = rs?.getString(p1) ?: return null
        return objectMapper.readValue(json, returnedClass())
    }

    override fun nullSafeSet(
        st: PreparedStatement,
        value: Map<String, Any>?,
        index: Int,
        session: SharedSessionContractImplementor?
    ) {
        if (value == null) {
            st.setNull(index, Types.OTHER)
        } else {
            st.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER)
        }
    }

    override fun deepCopy(value: Map<String, Any>?): Map<String, Any>? {
        return if (value == null) null else HashMap(value)
    }

    override fun isMutable(): Boolean {
        return true
    }

    override fun disassemble(value: Map<String, Any>?): Serializable {
        return objectMapper.writeValueAsString(value) as Serializable
    }

    override fun assemble(cached: Serializable?, owner: Any?): Map<String, Any>? {
        return if (cached == null) null else objectMapper.readValue(cached.toString(), returnedClass())
    }

    override fun replace(original: Map<String, Any>?, target: Map<String, Any>?, owner: Any?): Map<String, Any>? {
        return deepCopy(original)
    }
}