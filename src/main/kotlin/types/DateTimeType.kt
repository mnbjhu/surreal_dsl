package types

import core.SurrealComparable
import core.TypeProducer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer

open class DateTimeType(): Primitive<LocalDateTime>(), SurrealComparable<LocalDateTime> {

    override val serializer: KSerializer<LocalDateTime> = LocalDateTime.serializer()

    override fun createReference(reference: String): DateTimeType {
        return DateTimeType().withReference(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "datetime")

    companion object: TypeProducer<LocalDateTime, DateTimeType>(DateTimeType())
}