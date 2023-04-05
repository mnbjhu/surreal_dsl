package types

import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class BooleanType(): Primitive<Boolean>() {
    override val serializer: KSerializer<Boolean> = Boolean.serializer()

    override fun createReference(reference: String): BooleanType {
        return BooleanType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "boolean")

    companion object: TypeProducer<Boolean, BooleanType>(BooleanType()){
        val TRUE = BooleanType.createReference("TRUE")
        val FALSE = BooleanType.createReference("FALSE")
    }
}
