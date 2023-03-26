package functions

import types.LongType
import types.ReturnType

fun count(value: ReturnType<*>) = LongType.createReference("count(${value.reference})")
