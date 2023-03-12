import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

class ResultListSerializer(private val results: List<KSerializer<Any?>>): KSerializer<List<Any?>> {
    private val resultsDescriptor = ListSerializer(JsonElement.serializer().nullable).descriptor

    override fun deserialize(decoder: Decoder): List<Any?> {
        val context = decoder.beginStructure(descriptor)
        val r = mutableListOf<Any?>()
        while (true){
            val index = context.decodeElementIndex(descriptor)
            if(index == DECODE_DONE) break
            r.add(context.decodeSerializableElement(descriptor, index, results[index]))
        }
        context.endStructure(descriptor)
        return r.toList()
    }


    override val descriptor: SerialDescriptor = resultsDescriptor

    override fun serialize(encoder: Encoder, value: List<Any?>) {
        val context = encoder.beginStructure(descriptor)
        results.forEachIndexed { index, result ->
            context.encodeSerializableElement(descriptor, index, result, value[index])
        }
        context.endStructure(descriptor)
    }
}
