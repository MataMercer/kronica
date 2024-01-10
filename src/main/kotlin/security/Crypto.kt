package org.matamercer.security

import at.favre.lib.crypto.bcrypt.BCrypt

fun hashPassword(password: String): String {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray())
}

fun verifyPassword(submittedPassword: String, hashedPassword: String): Boolean {
   return BCrypt.verifyer().verify(submittedPassword.toCharArray(), hashedPassword.toCharArray()).verified
}