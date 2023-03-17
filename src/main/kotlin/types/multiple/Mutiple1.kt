package types.multiple

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import types.ReturnType

data class Multiple1<a, A: ReturnType<a>>(val col1: A, override val reference: String): ReturnType<Multiple1.Vec<a>>{
    data class Vec<a>(val col1: a)

    override val serializer: KSerializer<Vec<a>> = object: KSerializer<Vec<a>>{
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("result_set"){
            element("col1", col1.serializer.descriptor)
        }

        override fun deserialize(decoder: Decoder): Vec<a> {
            var data: a? = null
            decoder.decodeStructure(descriptor){
                while (true){
                    when(decodeElementIndex(descriptor)){
                        0 -> data = decodeSerializableElement(descriptor, 0, col1.serializer)
                        DECODE_DONE -> break
                        else -> { throw Exception("Failed to parse JSON response") }
                    }
                }
            }
            return Vec(data as a)
        }

        override fun serialize(encoder: Encoder, value: Vec<a>) {
            TODO("Not yet implemented")
        }

    }

    override fun createReference(reference: String): ReturnType<Vec<a>> {
        return Multiple1(col1, reference)
    }

    override fun getFieldDefinition(tableName: String): String {
        TODO("Not yet implemented")
    }

}
