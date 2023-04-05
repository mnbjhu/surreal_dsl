package types.multiple

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import types.ReturnType

data class Multiple1<a, A: ReturnType<a>>(val col1: A): ReturnType<a>{

    override val serializer: KSerializer<a> = object: KSerializer<a>{
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("result_set"){
            element("col1", col1.serializer.descriptor)
        }

        override fun deserialize(decoder: Decoder): a {
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
            return data as a
        }

        override fun serialize(encoder: Encoder, value: a) {
            TODO("Not yet implemented")
        }

    }
    override var reference: String? = null

    override fun createReference(reference: String): ReturnType<a> {
        return Multiple1(col1).withReference(reference)
    }


    override fun getFieldTypeBounds(): Map<String, String> {
        TODO("Not yet implemented")
    }

}
