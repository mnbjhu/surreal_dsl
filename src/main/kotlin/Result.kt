import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

data class ResultSet<T>(val result: T, val status: String, val time: String)

class ResultSetParser<T, U: KSerializer<T>>(result: U): KSerializer<ResultSet<T>> {
    private val timeParser = String.serializer()
    private val statusParser = String.serializer()
    private val dataParser = result

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("result_set"){
        element("time", timeParser.descriptor)
        element("status", statusParser.descriptor)
        element("result", dataParser.descriptor)
    }

    override fun deserialize(decoder: Decoder): ResultSet<T> {
        var data: T? = null
        var status: String? = null
        var time: String? = null
        decoder.decodeStructure(descriptor){
            while (true){
                when(decodeElementIndex(descriptor)){
                    0 -> time = decodeSerializableElement(descriptor, 0, timeParser)
                    1 -> status = decodeSerializableElement(descriptor, 1, statusParser)
                    2 -> data = decodeSerializableElement(descriptor, 2, dataParser)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> { throw Exception("Failed to parse JSON response") }
                }
            }
        }
        return ResultSet(data as T, status!!, time!!)
    }
    override fun serialize(encoder: Encoder, value: ResultSet<T>) {
        val context = encoder.beginStructure(descriptor)
        context.encodeSerializableElement(descriptor, 0, dataParser, value.result)
        context.endStructure(descriptor)
    }
}
