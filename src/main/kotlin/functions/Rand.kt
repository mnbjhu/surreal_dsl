package functions

import types.*


fun rand() = DoubleType.createReference("rand()")
class Rand {

    fun bool() = BooleanType.createReference("rand::bool()")
    fun float() = DoubleType.createReference("rand::float()")
    fun <T, U: ReturnType<T>>enum(vararg values: U) = BooleanType.createReference("rand::enum(${values.joinToString(",") { it.reference!! }})")
    fun guid() = StringType.createReference("rand::guid()")
    fun uuid() = StringType.createReference("rand::uuid()")
    fun int() = LongType.createReference("rand::int()")
    fun string() = DoubleType.createReference("rand::string()")
    fun string(length: LongType) = DoubleType.createReference("rand::string(${length.reference})")
    fun string(length: Long) = DoubleType.createReference("rand::string($length)")
    fun string(minLength: LongType, maxLength: LongType) = DoubleType.createReference("rand::string(${minLength.reference},${maxLength.reference})")
    fun string(minLength: Long, maxLength: LongType) = DoubleType.createReference("rand::string($minLength,${maxLength.reference})")
    fun string(minLength: LongType, maxLength: Long) = DoubleType.createReference("rand::string(${minLength.reference},$maxLength)")
    fun string(minLength: Long, maxLength: Long) = DoubleType.createReference("rand::string($minLength,$maxLength)")

    fun time() = DateTimeType.createReference("rand::time()")

    fun time(minTime: LongType, maxTime: LongType) = DateTimeType.createReference("rand::time(${minTime.reference},${maxTime.reference})")
    fun time(minTime: Long, maxTime: LongType) = DateTimeType.createReference("rand::time($minTime,${maxTime.reference})")
    fun time(minTime: LongType, maxTime: Long) = DateTimeType.createReference("rand::time(${minTime.reference},$maxTime)")
    fun time(minTime: Long, maxTime: Long) = DateTimeType.createReference("rand::time($minTime,$maxTime)")
}