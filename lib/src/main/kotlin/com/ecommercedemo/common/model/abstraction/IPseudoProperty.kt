package com.ecommercedemo.common.model.abstraction

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor

interface IPseudoProperty {
    var entitySimpleName: String
    var key: String
    var typeDescriptor: TypeDescriptor
}