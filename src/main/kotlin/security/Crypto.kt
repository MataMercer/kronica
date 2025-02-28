package org.matamercer.security

import at.favre.lib.crypto.bcrypt.BCrypt
import org.apache.commons.codec.digest.HmacUtils

fun hashPassword(password: String): String {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray())
}

fun verifyPassword(submittedPassword: String, hashedPassword: String): Boolean {
   return BCrypt.verifyer().verify(submittedPassword.toCharArray(), hashedPassword.toCharArray()).verified
}

fun generateCsrfToken(sessionId: String): String {
    return HmacUtils("HmacMD5", "secretkey" ).hmacHex(sessionId)
}