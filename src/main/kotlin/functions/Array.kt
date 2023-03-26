package functions

import data.surrealJson
import kotlinx.serialization.serializer
import types.BooleanType
import types.LongType
import types.ReturnType
import types.SurrealArray

object Array {
    fun <T, U: ReturnType<T>>combine(first: SurrealArray<T, U>, second: SurrealArray<T, U>) = first.createReference("array::combine(${first.reference},${second.reference})")
    fun <T, U: ReturnType<T>>concat(first: SurrealArray<T, U>, second: SurrealArray<T, U>) = first.createReference("array::concat(${first.reference},${second.reference})")
    fun <T, U: ReturnType<T>>difference(first: SurrealArray<T, U>, second: SurrealArray<T, U>) = first.createReference("array::difference(${first.reference},${second.reference})")
    fun <T, U: ReturnType<T>>distinct(first: SurrealArray<T, U>) = first.createReference("array::distinct(${first.reference})")
    fun <T, U: ReturnType<T>>intersect(first: SurrealArray<T, U>, second: SurrealArray<T, U>) = first.createReference("array::intersect(${first.reference},${second.reference})")

    fun <T, U: ReturnType<T>>combine(first: SurrealArray<T, U>, second: List<T>) = first.createReference("array::combine(${first.reference},${surrealJson.encodeToString(first.serializer, second)})")
    fun <T, U: ReturnType<T>>concat(first: SurrealArray<T, U>, second: List<T>) = first.createReference("array::concat(${first.reference},${surrealJson.encodeToString(first.serializer, second)})")
    fun <T, U: ReturnType<T>>difference(first: SurrealArray<T, U>, second: List<T>) = first.createReference("array::difference(${first.reference},${surrealJson.encodeToString(first.serializer, second)})")
    fun <T, U: ReturnType<T>>intersect(first: SurrealArray<T, U>, second: List<T>) = first.createReference("array::intersect(${first.reference},${surrealJson.encodeToString(first.serializer, second)})")
    fun len(array: SurrealArray<*, *>) = LongType.createReference("array::len(${array.reference})")

    object Sort {
        fun <T, U: ReturnType<T>>asc(first: SurrealArray<T, U>) = first.createReference("array::sort::acs(${first.reference})")
        fun <T, U: ReturnType<T>>desc(first: SurrealArray<T, U>) = first.createReference("array::sort::desc(${first.reference})")
    }
}