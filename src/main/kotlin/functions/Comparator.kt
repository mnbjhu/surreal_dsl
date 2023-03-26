package functions

import SurrealComparable
import data.surrealJson
import types.BooleanType

infix fun <T>SurrealComparable<T>.lessThan(other: SurrealComparable<T>) = BooleanType.createReference("($reference < ${other.reference})")
infix fun <T>SurrealComparable<T>.greaterThan(other: SurrealComparable<T>) = BooleanType.createReference("($reference > ${other.reference})")
infix fun <T>SurrealComparable<T>.lessThanOrEqualTo(other: SurrealComparable<T>) = BooleanType.createReference("($reference <= ${other.reference})")
infix fun <T>SurrealComparable<T>.greaterThanOrEqualTo(other: SurrealComparable<T>) = BooleanType.createReference("($reference >= ${other.reference})")

infix fun <T>SurrealComparable<T>.lessThan(other: T) = BooleanType.createReference("($reference < ${surrealJson.encodeToString(serializer, other)})")
infix fun <T>SurrealComparable<T>.greaterThan(other: T) = BooleanType.createReference("($reference > ${surrealJson.encodeToString(serializer, other)})")
infix fun <T>SurrealComparable<T>.lessThanOrEqualTo(other: T) = BooleanType.createReference("($reference <= ${surrealJson.encodeToString(serializer, other)})")
infix fun <T>SurrealComparable<T>.greaterThanOrEqualTo(other: T) = BooleanType.createReference("($reference >= ${surrealJson.encodeToString(serializer, other)})")
