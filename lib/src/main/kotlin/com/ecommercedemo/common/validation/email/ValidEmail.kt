package com.ecommercedemo.userservice.validation.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


@Email(message = "Email should be valid")
@NotBlank(message = "Email is mandatory")
@Size(max = 100, message = "Email must be less than 100 characters")
annotation class ValidEmail
