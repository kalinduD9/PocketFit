package com.kalindu.pocketfit

import com.kalindu.pocketfit.utils.AuthValidation
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun validAuthenticationInputsHaveNoErrors() {
        assertNull(AuthValidation.nameError("Alex Smith"))
        assertNull(AuthValidation.emailError("alex@example.com"))
        assertNull(AuthValidation.passwordError("secret1"))
        assertNull(AuthValidation.confirmPasswordError("secret1", "secret1"))
    }

    @Test
    fun invalidAuthenticationInputsReturnErrors() {
        assertNotNull(AuthValidation.nameError(" "))
        assertNotNull(AuthValidation.emailError("invalid-email"))
        assertNotNull(AuthValidation.passwordError("123"))
        assertNotNull(AuthValidation.confirmPasswordError("secret1", "secret2"))
    }
}
