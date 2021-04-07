package cn.coegle18.smallsteps.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESUtil {
    companion object {
        @kotlin.ExperimentalStdlibApi
        fun encrypt(data: String, key: String, iv: String): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            val ivSpec = IvParameterSpec(iv.encodeToByteArray())
            val sKeySpec = SecretKeySpec(key.encodeToByteArray(), "AES")

            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, ivSpec)
            val encrypted = cipher.doFinal(data.encodeToByteArray())
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }

        @kotlin.ExperimentalStdlibApi
        fun decrypt(data: String, key: String, iv: String): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            val sKeySpec = SecretKeySpec(key.encodeToByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.encodeToByteArray())

            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivSpec)
            return String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)))
        }

        private val chars = ('a'..'z') + ('A'..'Z') + ('2'..'8')
        private fun getRandomString(length: Int): String = List(length) { chars.random() }.joinToString("")

        @ExperimentalStdlibApi
        fun getEncryptedPassword(plainPass: String, key: String): String {
            return encrypt(getRandomString(64) + plainPass, key, getRandomString(16))
        }
    }
}