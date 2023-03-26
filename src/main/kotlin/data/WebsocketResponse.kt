package data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

sealed class WebsocketResponse<T> {
    abstract val id: Long
    data class Error<T>(override val id: Long, val message: String): WebsocketResponse<T>()
    data class Success<T>(override val id: Long, val result: T): WebsocketResponse<T>()
}

class WebSocketResponseSerializer<T>(private val inner: KSerializer<T>): KSerializer<WebsocketResponse<T>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ws_response"){
        element("id", String.serializer().descriptor)
        element("result", inner.descriptor)
    }

    override fun deserialize(decoder: Decoder): WebsocketResponse<T> {
        var id: Long? = null
        var result: T? = null
        decoder.decodeStructure(descriptor){
            while (true) {
                when(decodeElementIndex(descriptor)){
                    0 -> id = decodeLongElement(descriptor, 0)
                    1 -> result = decodeSerializableElement(descriptor, 1, inner)
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
        }
        return WebsocketResponse.Success(id!!, result!!)
    }
    override fun serialize(encoder: Encoder, value: WebsocketResponse<T>) {
        TODO("Not yet implemented")
    }
}