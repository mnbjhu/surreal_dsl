package functions

import data.surrealJson
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.serializer
import types.DateTimeType
import types.DurationType
import types.LongType
import kotlin.time.Duration

object Time {

    fun floor(value: DateTimeType, duration: DurationType) = DateTimeType.createReference("time::floor(${value.reference}, ${duration.reference})")
    fun floor(value: LocalDateTime, duration: DurationType) = DateTimeType.createReference("time::floor(${surrealJson.encodeToString(LocalDateTime.serializer(), value)}, ${duration.reference})")
    fun floor(value: DateTimeType, duration: Duration) = DateTimeType.createReference("time::floor(${value.reference}, ${surrealJson.encodeToString( Duration.serializer(), duration)})")

    fun ceil(value: DateTimeType, duration: DurationType) = DateTimeType.createReference("time::ceil(${value.reference}, ${duration.reference})")
    fun ceil(value: DateTimeType, duration: Duration) = DateTimeType.createReference("time::ceil(${value.reference}, ${surrealJson.encodeToString( Duration.serializer(), duration)})")
    fun ceil(value: LocalDateTime, duration: DurationType) = DateTimeType.createReference("time::ceil(${surrealJson.encodeToString(LocalDateTime.serializer(), value)}, ${duration.reference})")

    fun round(value: DateTimeType, duration: DurationType) = DateTimeType.createReference("time::round(${value.reference}, ${duration.reference})")
    fun round(value: DateTimeType, duration: Duration) = DateTimeType.createReference("time::round(${value.reference}, ${surrealJson.encodeToString( Duration.serializer(), duration)})")
    fun round(value: LocalDateTime, duration: DurationType) = DateTimeType.createReference("time::round(${surrealJson.encodeToString(LocalDateTime.serializer(), value)}, ${duration.reference})")

    fun group(value: DateTimeType, group: Group)= DateTimeType.createReference("time::group(${value.reference},${group.text})")

    fun unix(value: DateTimeType) = LongType.createReference("time::unix(${value.reference})")
    fun nano(value: DateTimeType) = LongType.createReference("time::nano(${value.reference})")
    fun secs(value: DateTimeType) = LongType.createReference("time::secs(${value.reference})")
    fun mins(value: DateTimeType) = LongType.createReference("time::secs(${value.reference})")
    fun hour(value: DateTimeType) = LongType.createReference("time::hour(${value.reference})")
    fun day(value: DateTimeType) = LongType.createReference("time::day(${value.reference})")
    fun week(value: DateTimeType) = LongType.createReference("time::week(${value.reference})")
    fun month(value: DateTimeType) = LongType.createReference("time::month(${value.reference})")
    fun year(value: DateTimeType) = LongType.createReference("time::year(${value.reference})")
    fun wday(value: DateTimeType) = LongType.createReference("time::wday(${value.reference})")
    fun yday(value: DateTimeType) = LongType.createReference("time::wday(${value.reference})")

    fun now(value: DateTimeType) = LongType.createReference("time::now()")

    enum class Group(val text: String){
        Second("second"),
        Minute("minute"),
        Hour("hour"),
        Day("day"),
        Month("month"),
        Year("year")
    }

}

