package functions

import types.BooleanType
import types.ReturnType
import types.StringType

object Crypto {
    fun md5(value: ReturnType<*>) = value.createReference("md5(${value.reference})")
    fun sha1(value: ReturnType<*>) = value.createReference("sha1(${value.reference})")
    fun sha256(value: ReturnType<*>) = value.createReference("sha256(${value.reference})")
    fun sha512(value: ReturnType<*>) = value.createReference("sha512(${value.reference})")

    object Argon2 {
        fun compare(hash: StringType, pass: StringType) = BooleanType.createReference("crypto::argon2::compare(${hash.reference},${pass.reference})")
        fun generate(pass: StringType) = StringType.createReference("crypto::argon2::generate(${pass.reference})")
    }

    object Pbkdf2 {
        fun compare(hash: StringType, pass: StringType) = BooleanType.createReference("crypto::pbkdf2::compare(${hash.reference},${pass.reference})")
        fun generate(pass: StringType) = StringType.createReference("crypto::pbkdf::generate(${pass.reference})")
    }

    object Scrypt {
        fun compare(hash: StringType, pass: StringType) = BooleanType.createReference("crypto::scrypt::compare(${hash.reference},${pass.reference})")
        fun generate(pass: StringType) = StringType.createReference("crypto::scrypt::generate(${pass.reference})")
    }
}