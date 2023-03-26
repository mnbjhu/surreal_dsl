package functions

import types.DoubleType
import types.LongType
import types.ReturnType
import types.SurrealArray
import kotlin.jvm.internal.Ref.LongRef

object Math {
    fun abs(number: DoubleType) = DoubleType.createReference("math::abs(${number.reference})")
    fun abs(number: LongType) = LongType.createReference("math::abs(${number.reference})")
    fun round(number: DoubleType) = LongType.createReference("math::round(${number.reference})")
    fun ceil(number: DoubleType) = LongType.createReference("math::ceil(${number.reference})")
    fun floor(number: DoubleType) = LongType.createReference("math::floor(${number.reference})")
    fun fixed(number: DoubleType, decimalPlaces: LongType) = LongType.createReference("math::fixed(${number.reference}, ${decimalPlaces.reference})")
    fun fixed(number: Double, decimalPlaces: LongType) = LongType.createReference("math::fixed($number, ${decimalPlaces.reference})")
    fun fixed(number: DoubleType, decimalPlaces: Long) = LongType.createReference("math::fixed(${number.reference}, $decimalPlaces)")
    fun max(array: SurrealArray<Double, DoubleType>) = array.inner.createReference("array::max(${array.reference})")
    fun min(array: SurrealArray<Double, DoubleType>) = array.inner.createReference("array::min(${array.reference})")
    fun max(array: SurrealArray<Long, LongType>) = array.inner.createReference("array::max(${array.reference})")
    fun min(array: SurrealArray<Long, LongType>) = array.inner.createReference("array::min(${array.reference})")
    fun mean(array: SurrealArray<Double, DoubleType>) = DoubleType.createReference("array::mean(${array.reference})")
    fun mean(array: SurrealArray<Long, LongType>) = DoubleType.createReference("array::mean(${array.reference})")
    fun median(array: SurrealArray<Double, DoubleType>) = DoubleType.createReference("array::median(${array.reference})")
    fun median(array: SurrealArray<Long, LongType>) = LongType.createReference("array::median(${array.reference})")
    fun product(array: SurrealArray<Double, DoubleType>) = DoubleType.createReference("array::product(${array.reference})")
    fun product(array: SurrealArray<Long, LongType>) = LongType.createReference("array::product(${array.reference})")
    fun sqrt(number: DoubleType) = DoubleType.createReference("math::sqrt(${number.reference})")
    fun sqrt(number: LongType) = DoubleType.createReference("math::sqrt(${number.reference})")
    fun sum(array: SurrealArray<Double, DoubleType>) = DoubleType.createReference("array::sum(${array.reference})")
    fun sum(array: SurrealArray<Long, LongType>) = LongType.createReference("array::sum(${array.reference})")
}