package com.ecommercedemo.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class JsonbUserType <T : Any>(private val clazz: Class<T>): UserType<Any> {

    private val objectMapper = ObjectMapper()

    override fun getSqlType(): Int {
        return Types.OTHER
    }

    @Suppress("UNCHECKED_CAST")
    override fun returnedClass(): Class<Any> = clazz as Class<Any>

    override fun nullSafeGet(
        p0: ResultSet?,
        p1: Int,
        p2: SharedSessionContractImplementor?,
        p3: Any?
    ): Any? {
        val json = p0?.getString(p1)
        return if (json != null) objectMapper.readValue(json, returnedClass()) else null
    }

    override fun nullSafeSet(st: PreparedStatement, value: Any?, index: Int, session: SharedSessionContractImplementor?) {
        if (value == null) {
            st.setNull(index, Types.OTHER)
        } else {
            st.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER)
        }
    }

    override fun deepCopy(value: Any?): Any? {
        return if (value == null) null else objectMapper.readValue(objectMapper.writeValueAsString(value), returnedClass())
    }

    override fun isMutable(): Boolean {
        return true
    }

    override fun disassemble(value: Any?): Serializable {
        return deepCopy(value) as Serializable
    }

    override fun assemble(cached: Serializable?, owner: Any?): Any? {
        return deepCopy(cached)
    }

    override fun replace(original: Any?, target: Any?, owner: Any?): Any? {
        return deepCopy(original)
    }

    override fun equals(x: Any?, y: Any?): Boolean {
        return x == y
    }

    override fun hashCode(x: Any?): Int {
        return x?.hashCode() ?: 0
    }
}