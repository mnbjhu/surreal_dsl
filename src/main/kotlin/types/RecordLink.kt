package types

import RecordType
import core.Table
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

class RecordLink<T, out U: RecordType<T>>(override val reference: String, private val inner: U, val hasDefinition: Boolean = true):
    ReturnType<String> {
    val o: U
        get() = inner.createReference(reference) as U
    companion object {
        fun <T, U: RecordType<T>> Table<T, U>.idOf(key: String) = RecordLink<T, U>("$name:$key", inner)
    }

    override val serializer: KSerializer<String> = String.serializer()

    override fun createReference(reference: String): ReturnType<String> = RecordLink(reference, inner, hasDefinition)

    override fun getFieldDefinition(tableName: String): String {

        return if(hasDefinition) "DEFINE FIELD $reference ON $tableName TYPE record(${inner.reference});" else ""
    }
}


fun <T, U: RecordType<T>>recordLink(to: U) = TypeProducer(RecordLink("dummy", to))
fun <T, U: RecordType<T>>recordLink(to: Table<T, U>) = TypeProducer(RecordLink("dummy", to.inner))
fun <T, U: RecordType<T>>idOf(to: U) = TypeProducer(RecordLink("dummy", to, hasDefinition = false))

/*
sealed class RecordId<T> {
    class Reference: RecordId<*>()
    class : RecordId<*>()
}

 */
