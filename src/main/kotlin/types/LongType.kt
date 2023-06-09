package types

import core.SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class LongType(): Primitive<Long>(), SurrealComparable<Long> {

    override val serializer: KSerializer<Long> = Long.serializer()

    override fun createReference(reference: String): LongType {
        return LongType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "int")
    companion object: TypeProducer<Long, LongType>(LongType())
}