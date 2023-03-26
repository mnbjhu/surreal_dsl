package types

import SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class StringType(override val reference: String): ReturnType<String>, SurrealComparable<String> {

    override val serializer: KSerializer<String> = String.serializer()

    override fun createReference(reference: String): ReturnType<String> {
        return StringType(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "string")

    companion object: TypeProducer<String, StringType>(StringType("dummy"))
}



