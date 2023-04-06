package types

import core.SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class DoubleType(): Primitive<Double>(), SurrealComparable<Double> {

    override val serializer: KSerializer<Double> = Double.serializer()

    override fun createReference(reference: String): DoubleType {
        return DoubleType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "float")

    operator fun plus(other: DoubleType) = DoubleType.createReference("($reference + ${other.reference})")
    operator fun plus(other: LongType) = DoubleType.createReference("($reference + ${other.reference})")
    operator fun plus(other: Number) = DoubleType.createReference("($reference + $other)")

    operator fun minus(other: DoubleType) = DoubleType.createReference("($reference - ${other.reference})")
    operator fun minus(other: LongType) = DoubleType.createReference("($reference - ${other.reference})")
    operator fun minus(other: Number) = DoubleType.createReference("($reference - $other)")

    operator fun times(other: DoubleType) = DoubleType.createReference("($reference * ${other.reference})")
    operator fun times(other: LongType) = DoubleType.createReference("($reference * ${other.reference})")
    operator fun times(other: Number) = DoubleType.createReference("($reference * $other)")

    operator fun div(other: DoubleType) = DoubleType.createReference("($reference / ${other.reference})")
    operator fun div(other: LongType) = DoubleType.createReference("($reference / ${other.reference})")
    operator fun div(other: Number) = DoubleType.createReference("($reference / $other)")
    companion object: TypeProducer<Double, DoubleType>(DoubleType())
}