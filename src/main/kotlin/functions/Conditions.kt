package functions

import data.surrealJson
import types.BooleanType
import types.ReturnType

infix fun <T>ReturnType<T>.eq(other: T) = BooleanType.createReference("($reference == ${ surrealJson.encodeToString(serializer, other) })")

infix fun <T, U: ReturnType<T>>U.eq(other: U) = BooleanType.createReference("($reference == ${other.reference})")

infix fun BooleanType.and(other: BooleanType): BooleanType = BooleanType.createReference("($reference) AND (${other.reference})")
infix fun BooleanType.or(other: BooleanType): BooleanType = BooleanType.createReference("($reference) OR (${other.reference})")


