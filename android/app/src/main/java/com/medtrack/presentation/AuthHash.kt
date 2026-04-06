package com.medtrack.presentation

import java.security.MessageDigest

internal fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(toByteArray())
    return hash.joinToString("") { byte -> "%02x".format(byte) }
}

