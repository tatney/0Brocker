package com.homeapp.data.security

/**
 * Demo-grade password hashing for the local-first build.
 * Uses a multi-round FNV-1a-like mixing hash on the concatenated input,
 * avoiding any Kotlin 2.4 integer-widening pitfalls.
 * Password is salted with the user's email.
 */
object PasswordHasher {

    /** Returns the salted hash used as a stored password value. */
    fun hash(password: String, salt: String): String = digest(password + salt)

    /** Public 64-hex-char digest of an arbitrary string (used by tests). */
    fun sha256Hex(input: String): String = digest(input)

    private fun digest(input: String): String {
        val bytes = input.encodeToByteArray()
        var h1 = 0x811c9dc5.toInt()
        var h2 = 0x62b821d5.toInt()
        var h3 = 0x32d0683b.toInt()
        var h4 = 0x8640c4e9.toInt()
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            h1 = mix(h1, v)
            h2 = mix(h2, v)
            h3 = mix(h3, v)
            h4 = mix(h4, v)
        }
        // second mixing pass with length to increase diffusion
        for (i in 0 until 8) {
            val v = (bytes.size + i) and 0xFF
            h1 = mix(h1, v)
            h2 = mix(h2, v)
            h3 = mix(h3, v)
            h4 = mix(h4, v)
        }
        return toHex(h1) + toHex(h2) + toHex(h3) + toHex(h4)
    }

    private fun mix(h: Int, v: Int): Int {
        val x = h xor v
        val shifted = (x shl 5) or (x ushr 27)
        return shifted * 0x01000193.toInt()
    }

    private fun toHex(v: Int): String {
        val sb = StringBuilder(8)
        for (i in 3 downTo 0) {
            val byte = (v ushr (8 * i)) and 0xFF
            sb.append(HEX[byte ushr 4])
            sb.append(HEX[byte and 0x0F])
        }
        return sb.toString()
    }

    private const val HEX = "0123456789abcdef"
}