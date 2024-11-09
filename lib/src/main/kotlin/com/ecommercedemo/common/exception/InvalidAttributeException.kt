package com.ecommercedemo.common.exception

class InvalidAttributeException(attributePath: String, entityName: String) :
    RuntimeException("Attribute '$attributePath' does not exist in $entityName")