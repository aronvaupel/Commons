package com.ecommercedemo.common.application.validation.type

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ValueTypeTest {

    @Test
    fun `BYTE validates correct values`() {
        val descriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.BYTE, isNullable = false)

        assertTrue(ValueType.BYTE.validate(10, descriptor))
        assertTrue(ValueType.BYTE.validate("127", descriptor))
        assertFalse(ValueType.BYTE.validate("invalid", descriptor))
    }

    @Test
    fun `UBYTE validates unsigned values`() {
        val descriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.UBYTE, isNullable = false)
        assertTrue(ValueType.UBYTE.validate("255", descriptor))
        assertFalse(ValueType.UBYTE.validate("-1", descriptor))
    }

    @Test
    fun `BOOLEAN validates boolean values`() {
        val descriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.BOOLEAN, isNullable = false)

        assertTrue(ValueType.BOOLEAN.validate(true, descriptor))
        assertTrue(ValueType.BOOLEAN.validate("true", descriptor))
        assertTrue(ValueType.BOOLEAN.validate("FALSE", descriptor))
        assertFalse(ValueType.BOOLEAN.validate("not-a-boolean", descriptor))
    }

    @Test
    fun `STRING validates string values`() {
        val descriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.STRING, isNullable = false)

        assertTrue(ValueType.STRING.validate("validString", descriptor))
        assertFalse(ValueType.STRING.validate(123, descriptor))
    }

    @Test
    fun `BIG_INTEGER validates big integer values`() {
        val descriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.BIG_INTEGER, isNullable = false)

        assertTrue(ValueType.BIG_INTEGER.validate(BigInteger("123456789"), descriptor))
        assertTrue(ValueType.BIG_INTEGER.validate("987654321", descriptor))
        assertFalse(ValueType.BIG_INTEGER.validate("invalid", descriptor))
    }

    @Test
    fun `INSTANT validates time strings`() {
        val descriptor = TypeDescriptor.TimeDescriptor(type = ValueType.INSTANT, isNullable = false)

        assertTrue(ValueType.INSTANT.validate("2023-01-01T10:00:00Z", descriptor))
        assertFalse(ValueType.INSTANT.validate("invalid-date", descriptor))
    }

    @Test
    fun `LIST validates list values`() {
        val itemDescriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.INTEGER, isNullable = false)
        val descriptor = TypeDescriptor.CollectionDescriptor(
            type = ValueType.LIST,
            itemDescriptor = itemDescriptor,
            minElements = 1,
            maxElements = 5
        )

        assertTrue(ValueType.LIST.validate(listOf(1, 2, 3), descriptor))
        assertFalse(ValueType.LIST.validate(listOf(1, "invalid", 3), descriptor))
    }

    @Test
    fun `MAP validates map values`() {
        val keyDescriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.STRING, isNullable = false)
        val valueDescriptor = TypeDescriptor.PrimitiveDescriptor(type = ValueType.INTEGER, isNullable = false)
        val descriptor = TypeDescriptor.MapDescriptor(
            type = ValueType.MAP,
            keyDescriptor = keyDescriptor,
            valueDescriptor = valueDescriptor,
            minEntries = 1,
            maxEntries = 3
        )

        assertTrue(ValueType.MAP.validate(mapOf("key1" to 1, "key2" to 2), descriptor))
        assertFalse(ValueType.MAP.validate(mapOf("key1" to "invalid"), descriptor))
    }

    @Test
    fun `ENUM validates enum values`() {
        val descriptor = TypeDescriptor.EnumDescriptor(
            type = ValueType.ENUM,
            isNullable = false,
            enumValues = listOf("VALUE1", "VALUE2", "VALUE3")
        )

        assertTrue(ValueType.ENUM.validate("VALUE1", descriptor))
        assertFalse(ValueType.ENUM.validate("INVALID_VALUE", descriptor))
    }

    @Test
    fun `validateComplexObject validates nested fields`() {
        val fieldDescriptors = mapOf(
            "field1" to TypeDescriptor.PrimitiveDescriptor(type = ValueType.STRING, isNullable = false),
            "field2" to TypeDescriptor.PrimitiveDescriptor(type = ValueType.INTEGER, isNullable = false)
        )
        val descriptor = TypeDescriptor.ComplexObjectDescriptor(
            type = ValueType.OBJECT,
            isNullable = false,
            fields = fieldDescriptors
        )

        assertTrue(ValueType.OBJECT.validate(mapOf("field1" to "value", "field2" to 123), descriptor))
        assertFalse(ValueType.OBJECT.validate(mapOf("field1" to "value", "field2" to "invalid"), descriptor))
    }

}
