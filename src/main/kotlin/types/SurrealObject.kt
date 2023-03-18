package types

import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

abstract class SurrealObject<T>(override val reference: String):
    ReturnType<T> {

    val members: List<Pair<String, ReturnType<*>>> =
         this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                        it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) it.name to value
                else null
            }
    override val serializer: KSerializer<T> =
        object: KSerializer<T>{
            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor(reference){
                    members.forEach {
                        element(it.first, it.second.serializer.descriptor)
                    }
                }

            override fun deserialize(decoder: Decoder): T {
                val values = mutableMapOf<String, Any?>()
                decoder.decodeStructure(descriptor){
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
    override fun getFieldDefinition(tableName: String): String {
       return "DEFINE FIELD $reference.* ON $tableName TYPE object;\n" + this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                    it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) value
                else null
            }
            .joinToString("\n") { it.getFieldDefinition(tableName) }
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