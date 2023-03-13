import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ResultListSerializer(private val results: List<KSerializer<ResultSet<Any?>>>): KSerializer<List<Any?>> {
    @OptIn(InternalSerializationApi::class)
    private val resultsDescriptor = buildSerialDescriptor("results", StructureKind.LIST){

        results.forEachIndexed { index, it ->
            element("$index", it.descriptor)
        }
    }

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
        /*
        val context = encoder.beginStructure(descriptor)
        results.forEachIndexed { index, result ->
            context.encodeSerializableElement(descriptor, index, result, ResultSet(value[index]))
        }
        context.endStructure(descriptor)

         */
        TODO()
    }
}
