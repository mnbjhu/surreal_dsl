package types

import annotation.SurrealDsl
import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

@SurrealDsl
abstract class SurrealObject<T>(override val reference: String):
    ReturnType<T> {

    val members: List<Pair<String, ReturnType<*>>> by lazy {
        this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                        it.parameters.size == 1
            }
            .mapNotNull {
        try {
            it.call(this)
        } catch (e: InvocationTargetException){
            throw e.targetException
        }
                val value = it.call(this)
                if(value is ReturnType<*>) it.name to value
                else null
            }
    }
    override val serializer: KSerializer<T>
        get() =
        object: KSerializer<T>{
            override val descriptor: SerialDescriptor by lazy {
                buildClassSerialDescriptor(reference){
                    members.forEach {
                        if(it.second is RecordLink<*, *>){
                            element(it.first, String.serializer().descriptor)
                        } else element(it.first, it.second.serializer.descriptor)
                    }
                }
            }

            override fun deserialize(decoder: Decoder): T {
                val values = mutableMapOf<String, Any?>()
                decoder.decodeStructure(descriptor) {
                    while (true){
                        when(val index = decodeElementIndex(descriptor)){
                            CompositeDecoder.DECODE_DONE -> break;
                            else -> {
                                val param = members[index]
                                values[param.first] = decodeSerializableElement(descriptor, index, param.second.serializer)
                            }
                        }
                    }
                }
                return ReturnScope(values.toMap()).decode()
            }

            override fun serialize(encoder: Encoder, value: T) {
                encoder.encodeStructure(descriptor){
                    with(EncodeScope().apply { encode(value) }){
                        encode(descriptor)
                    }
                }
            }

        }
    override fun getFieldTypeBounds(): Map<String, String>
    {
        return (this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                    it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) it.name to value
                else null
            }
            .flatMap { f -> listOf("" to "object") + f.second.getFieldTypeBounds().entries.map { "${if(it.key != "") "${f.first}.${it.key}" else "${f.first}"}" to it.value} } )
            .toMap()
    }
    protected open operator fun <t, u: ReturnType<t>> TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference("${thisRef.reference}.${property.name}") as u
    }

    abstract fun ReturnScope.decode(): T

    abstract fun EncodeScope.encode(value: T)

    inner class EncodeScope(){

        private val params = mutableMapOf<String, Any?>()
        infix fun <T, U: ReturnType<T>>KProperty<U>.setAs(value: T){
            params[name] = value
        }
        fun CompositeEncoder.encode(descriptor: SerialDescriptor){
            members.forEachIndexed { index, pair ->
                params[pair.first]?.let {
                    encodeSerializableElement(descriptor, index, pair.second.serializer as KSerializer<Any?>, it)
                }
            }
        }
    }
}

class ReturnScope(private val params: Map<String, Any?>) {
    operator fun <T, U: ReturnType<T>>KProperty<U>.not(): T {
        return params[name] as T
    }
}


open class SurrealObjectSerializer()