package com.quibbler.sevenmusic.utils

import android.text.TextUtils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Util {
    private val HEX_DIGITS: CharArray? = charArrayOf(
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    )

    fun encodeStr2MD5(s: String): String? {
        if (TextUtils.isEmpty(s)) {
            return null
        }

        try {
            // 使用MD5创建MessageDigest对象
            val digest = MessageDigest.getInstance("MD5")

            digest.update(s.toByteArray())

            val messageDigest = digest.digest()

            return MD5Util.toHexString(messageDigest)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return s
    }

    private fun toHexString(b: ByteArray): String {
        val sb = StringBuilder(b.size * 2)

        for (i in b.indices) {
            sb.append(HEX_DIGITS!![(b[i].toInt() and 0xf0) ushr 4])
            sb.append(HEX_DIGITS[b[i].toInt() and 0x0f])
        }

        return sb.toString()
    }
}