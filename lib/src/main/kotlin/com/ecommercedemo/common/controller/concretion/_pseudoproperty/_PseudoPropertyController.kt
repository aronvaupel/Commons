package com.ecommercedemo.common.controller.concretion._pseudoproperty

import com.ecommercedemo.common.controller.abstraction.DownstreamRestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pseudo-properties")
@ControllerFor(_PseudoProperty::class)
@ConditionalOnProperty(name = ["pseudo-properties"], havingValue = "true", matchIfMissing = false)
@Suppress("ClassName")
class _PseudoPropertyController: DownstreamRestControllerTemplate<_PseudoProperty>()