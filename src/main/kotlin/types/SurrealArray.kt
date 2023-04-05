package types

import RecordType
import core.Linked
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

open class SurrealArray<T, U: ReturnType<T>>(internal val inner: U):
    ReturnType<List<T>> {
    override var reference: String? = null
    val o: U
        get() = inner.createReference(reference!!) as U
    override val serializer: KSerializer<List<T>> = ListSerializer(inner.serializer)
    override fun createReference(reference: String) = SurrealArray(inner).withReference(reference)

    operator fun plus(other: SurrealArray<T, U>): SurrealArray<T, U> = createReference("array::concat(($reference), (${other.reference}))")
/*
    fun <a, A: ReturnType<a>>map(transform: (U) -> A): SurrealArray<a, A>{
        val result = transform(inner)
        return SurrealArray(result, "$reference.${result.reference}")
    }

 */
    override fun getFieldTypeBounds(): Map<String, String> {
        val r = listOf("" to "array") + inner.getFieldTypeBounds().entries.map {
            it.key + "*" to it.value
        }
        return r.toMap()
    }

}
fun <T, U: ReturnType<T>>array(type: TypeProducer<T, U>): TypeProducer<List<T>, SurrealArray<T, U>> {
    return TypeProducer(SurrealArray(type.createReference("dummy")))
}
fun <a, A: ReturnType<a>,  T, U: RecordType<T>>SurrealArray<Linked<T>, RecordLink<T, U>>.linked(transform: U.() -> A): SurrealArray<a, A>{
    val result = inner.o.transform()
    return SurrealArray(result).withReference("$reference.${result.reference}")
}

