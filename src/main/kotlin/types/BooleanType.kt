package types

import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

open class BooleanType(override val reference: String): ReturnType<Boolean> {

    override val serializer: KSerializer<Boolean> = Boolean.serializer()

    override fun createReference(reference: String): ReturnType<Boolean> {
        return BooleanType(reference)
    }

    override fun getFieldDefinition(tableName: String): String =
        "DEFINE FIELD $reference ON $tableName TYPE boolean;"

    companion object {
        val TRUE = BooleanType("TRUE")
        val FALSE = BooleanType("FALSE")
    }
}

val booleanType = TypeProducer(BooleanType("dummy"))