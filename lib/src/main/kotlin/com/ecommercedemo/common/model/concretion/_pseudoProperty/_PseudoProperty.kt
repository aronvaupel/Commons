package com.ecommercedemo.common.model.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyEntityRegisteredCondition
import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.Type
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Conditional

@Suppress("unused", "ClassName")
@Entity
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
@Conditional(ExcludeIfPseudoPropertyEntityRegisteredCondition::class)
open class _PseudoProperty(
    @NotBlank
    @Column(name = "entity_simple_name", nullable = false)
    override var entitySimpleName: String = "",

    @NotBlank
    @Column(name = "key", nullable = false)
    override var key: String = "",

    @Type(JsonType::class)
    @Column(
        name = "type_descriptor",
        columnDefinition = "jsonb",
        nullable = false
    )
    override var typeDescriptor: TypeDescriptor = TypeDescriptor.PrimitiveDescriptor(
        category = "PRIMITIVE",
        type = ValueType.STRING,
        isNullable = true
    )
) : BaseEntity(), IPseudoProperty
