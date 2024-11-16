package com.ecommercedemo.common.application.exception

class InvalidAttributeException(attributePath: String, entityName: String) :
    RuntimeException("Attribute '$attributePath' does not exist in $entityName")