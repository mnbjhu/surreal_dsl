package types

import RecordType
import core.Linked
import core.Table
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*

class RecordLink<T, out U: RecordType<T>>(override val reference: String, private val inner: U, val hasDefinition: Boolean = true):
    ReturnType<Linked<T>> {
    val o: U
        get() = inner.createReference(reference) as U
    companion object {
        fun <T, U: RecordType<T>> Table<T, U>.idOf(key: String) = RecordLink<T, U>("$name:$key", inner)
    }

    override val serializer: KSerializer<Linked<T>> = object: KSerializer<Linked<T>>{
            override val descriptor: SerialDescriptor by lazy { inner.serializer.descriptor }

            override fun deserialize(decoder: Decoder): Linked<T> {
                return try {
                    Linked.Actual(inner.serializer.deserialize(decoder))
                } catch (e: Exception) {
                    val id = decoder.decodeString()
                    Linked.Reference(id)
                }
            }

            override fun serialize(encoder: Encoder, value: Linked<T>) {
                when(value){
                    is Linked.Actual -> inner.serializer.serialize(encoder, value.record)
                    is Linked.Reference -> encoder.encodeStructure(descriptor){ encodeSerializableElement(descriptor, 0, String.serializer(), value.name)}
                }
            }

        }

    override fun createReference(reference: String): ReturnType<Linked<T>> = RecordLink(reference, inner, hasDefinition)

    override fun getFieldTypeBounds(): Map<String, String> = if(hasDefinition) mapOf("" to "record(${inner.reference})") else mapOf()
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
