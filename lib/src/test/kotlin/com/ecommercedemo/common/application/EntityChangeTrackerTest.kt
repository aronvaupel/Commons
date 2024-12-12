package com.ecommercedemo.common.application

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import java.time.LocalDateTime
import java.util.*

data class MockEntity(
    override var id: UUID = UUID.randomUUID(),
    var name: String,
    var email: String?,
    var age: Int?,
    override var createdAt: LocalDateTime = LocalDateTime.now(),
    override var updatedAt: LocalDateTime = LocalDateTime.now(),
    override var pseudoProperties: Map<String, Any?> = mapOf()
) : AugmentableBaseEntity()

class EntityChangeTrackerTest {

//    private val tracker = EntityChangeTracker<MockEntity>(serviceUtility = ServiceUtility(
//        objectMapper = ObjectMapper(),
//        _pseudoPropertyRepository = PseudoPropertyRepository
//    ))
//
//    @Test
//    fun `should detect changes in non-nullable properties`() {
//        val entityBefore = MockEntity(
//            name = "Alice",
//            email = "alice@example.com",
//            age = 25
//        )
//        val entityAfter = entityBefore.copy(
//            name = "Alicia"
//        )
//
//        val changes = tracker.getChangedProperties(entityBefore, entityAfter)
//
//        assertEquals(1, changes.size)
//        assertEquals("Alicia", changes["name"])
//    }
//
//    @Test
//    fun `should detect changes in nullable properties`() {
//        val entityBefore = MockEntity(
//            name = "Bob",
//            email = "bob@example.com",
//            age = 30
//        )
//        val entityAfter = entityBefore.copy(
//            email = null
//        )
//
//        val changes = tracker.getChangedProperties(entityBefore, entityAfter)
//
//        assertEquals(1, changes.size)
//        assertEquals(null, changes["email"])
//    }
//
//    @Test
//    fun `should detect multiple property changes`() {
//        val entityBefore = MockEntity(
//            name = "Charlie",
//            email = "charlie@example.com",
//            age = 35
//        )
//        val entityAfter = entityBefore.copy(
//            name = "Charles",
//            age = 36
//        )
//
//        val changes = tracker.getChangedProperties(entityBefore, entityAfter)
//
//        assertEquals(2, changes.size)
//        assertEquals("Charles", changes["name"])
//        assertEquals(36, changes["age"])
//    }
//
//    @Test
//    fun `should detect no changes when properties are the same`() {
//        val entityBefore = MockEntity(
//            name = "Diana",
//            email = "diana@example.com",
//            age = 28
//        )
//        val entityAfter = entityBefore.copy()
//
//        val changes = tracker.getChangedProperties(entityBefore, entityAfter as MockEntity)
//
//        assertEquals(0, changes.size)
//    }
//
//    @Test
//    fun `should detect when a property is set to null and another property is changed`() {
//        val entityBefore = MockEntity(
//            name = "Eve",
//            email = "eve@example.com",
//            age = 40
//        )
//        val entityAfter = entityBefore.copy(
//            email = "eve.new@example.com",
//            age = null
//        )
//
//        val changes = tracker.getChangedProperties(entityBefore, entityAfter)
//
//        assertEquals(2, changes.size)
//        assertEquals("eve.new@example.com", changes["email"])
//        assertEquals(null, changes["age"])
//    }
}