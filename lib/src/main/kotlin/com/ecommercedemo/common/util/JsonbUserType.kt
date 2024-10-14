package com.ecommercedemo.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

@Suppress("UNCHECKED_CAST")
class JsonbUserType <T : Any>: UserType<T> {

    private val objectMapper = ObjectMapper()

    override fun getSqlType(): Int {
        return Types.OTHER
    }

    override fun returnedClass(): Class<T> = Any::class.java as Class<T>

    override fun nullSafeGet(
        p0: ResultSet?,
        p1: Int,
        p2: SharedSessionContractImplementor?,
        p3: Any?
    ): T? {
        val json = p0?.getString(p1)
        return if (json != null) objectMapper.readValue(json, returnedClass()) else null
    }

    override fun nullSafeSet(st: PreparedStatement, value: T?, index: Int, session: SharedSessionContractImplementor?) {
        if (value == null) {
            st.setNull(index, Types.OTHER)
        } else {
            st.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER)
        }
    }

    override fun deepCopy(value: T?): T? {
        return if (value == null) null else objectMapper.readValue(objectMapper.writeValueAsString(value), returnedClass())
    }

    override fun isMutable(): Boolean {
        return true
    }

    override fun disassemble(value: T?): Serializable {
        return deepCopy(value) as Serializable
    }

    override fun assemble(cached: Serializable?, owner: Any?): T? {
        return deepCopy(cached as T?)
    }

    override fun replace(original: T?, target: T?, owner: Any?): T? {
        return deepCopy(original)
    }

    override fun equals(x: T?, y: T?): Boolean {
        return x == y
    }

    override fun hashCode(x: T?): Int {
        return x?.hashCode() ?: 0
    }
}