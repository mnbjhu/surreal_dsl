package types

import SurrealComparable
import core.TypeProducer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer

open class DateTimeType(override val reference: String): ReturnType<LocalDateTime>, SurrealComparable<LocalDateTime> {

    override val serializer: KSerializer<LocalDateTime> = LocalDateTime.serializer()

    override fun createReference(reference: String): DateTimeType {
        return DateTimeType(reference)
    }

    override fun getFieldTypeBounds(): Map<String, String> = mapOf("" to "datetime")

    companion object: TypeProducer<LocalDateTime, DateTimeType>(DateTimeType("dummy"))
}