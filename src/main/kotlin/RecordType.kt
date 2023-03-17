import core.TypeProducer
import kotlinx.serialization.KSerializer
import types.*
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf


abstract class RecordType<T>(private val name: String, serializer: KSerializer<T>): SurrealObject<T>(serializer, name){
    abstract val id: RecordLink<T, RecordType<T>>
    protected override operator fun <t, u: ReturnType<t>> TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return if('.' !in thisRef.reference) createReference(property.name) as u
        else createReference("${thisRef.reference}.${property.name}")
    }

    fun getDefinition(): String {
        val fields = this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                        it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) value
                else null
            }
            .joinToString("\n") { it.getFieldDefinition(name) }
        return "DEFINE TABLE $name SCHEMAFULL;\n$fields"
    }
}

