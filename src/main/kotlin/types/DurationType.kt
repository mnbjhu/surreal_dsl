package types

import SurrealComparable
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Duration

open class DurationType(override val reference: String): ReturnType<Duration>, SurrealComparable<Duration> {

    override val serializer: KSerializer<Duration> = Duration.serializer()

    override fun createReference(reference: String): DurationType {
        return DurationType(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "duration")

    companion object: TypeProducer<Duration, DurationType>(DurationType("dummy"))
}