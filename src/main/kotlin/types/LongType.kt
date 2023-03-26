package types

import SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class LongType(override val reference: String): ReturnType<Long>, SurrealComparable<Long> {

    override val serializer: KSerializer<Long> = Long.serializer()

    override fun createReference(reference: String): LongType {
        return LongType(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "int")
    companion object: TypeProducer<Long, LongType>(LongType("dummy"))
}