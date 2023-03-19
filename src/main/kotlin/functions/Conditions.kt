package functions

import data.surrealJson
import types.BooleanType
import types.ReturnType
import types.booleanType

infix fun <T>ReturnType<T>.eq(other: T) =
    booleanType.createReference("($reference = ${ surrealJson.encodeToString(serializer, other) })")

infix fun <T, U: ReturnType<T>>U.eq(other: U) =
    booleanType.createReference("($reference = ${other.reference})")

infix fun BooleanType.and(other: BooleanType): BooleanType = booleanType.createReference("($reference) AND (${other.reference})")
