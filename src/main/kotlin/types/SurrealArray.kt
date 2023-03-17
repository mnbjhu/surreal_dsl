package types

import RecordType
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

open class SurrealArray<T, U: ReturnType<T>>(internal val inner: U, override val reference: String):
    ReturnType<List<T>> {
    val o: U
        get() = inner.createReference(reference) as U
    override val serializer: KSerializer<List<T>> = ListSerializer(inner.serializer)
    override fun createReference(reference: String) = SurrealArray(inner, reference)

    operator fun plus(other: SurrealArray<T, U>): SurrealArray<T, U> = createReference("array::concat($reference, ${other.reference})")
/*
    fun <a, A: ReturnType<a>>map(transform: (U) -> A): SurrealArray<a, A>{
        val result = transform(inner)
        return SurrealArray(result, "$reference.${result.reference}")
    }

 */
    override fun getFieldDefinition(tableName: String): String {
        return "DEFINE FIELD $reference ON $tableName TYPE array;\n" +
                inner.createReference("$reference.*").getFieldDefinition(tableName)
    }

}
fun <T, U: ReturnType<T>>array(type: TypeProducer<T, U>): TypeProducer<List<T>, SurrealArray<T, U>> {
    return TypeProducer(SurrealArray(type.createReference("dummy"), "dummy"))
}
fun <a, A: ReturnType<a>,  T, U: RecordType<T>>SurrealArray<String, RecordLink<T, U>>.linked(transform: U.() -> A): SurrealArray<a, A>{
    val result = inner.o.transform()
    return SurrealArray(result, "$reference.${result.reference}")
}
