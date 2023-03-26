package types

import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class BooleanType(override val reference: String): ReturnType<Boolean> {

    override val serializer: KSerializer<Boolean> = Boolean.serializer()

    override fun createReference(reference: String): ReturnType<Boolean> {
        return BooleanType(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "boolean")

    companion object: TypeProducer<Boolean, BooleanType>(BooleanType("dummy")){
        val TRUE = BooleanType("TRUE")
        val FALSE = BooleanType("FALSE")
    }
}
