package types

import core.SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Duration

open class DurationType(): Primitive<Duration>(), SurrealComparable<Duration> {

    override val serializer: KSerializer<Duration> = Duration.serializer()

    override fun createReference(reference: String): DurationType {
        return DurationType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "duration")

    companion object: TypeProducer<Duration, DurationType>(DurationType())
}