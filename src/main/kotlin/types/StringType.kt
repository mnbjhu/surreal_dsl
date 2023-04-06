package types

import core.SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

abstract class Primitive<T>: ReturnType<T> {
    override var reference: String? = null
}
open class StringType(): Primitive<String>(), SurrealComparable<String> {

    override val serializer: KSerializer<String> = String.serializer()

    override fun createReference(reference: String): ReturnType<String> {
        return StringType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "string")

    companion object: TypeProducer<String, StringType>(StringType())
}



