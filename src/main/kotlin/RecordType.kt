import core.Permission
import core.TypeProducer
import types.*
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf


abstract class RecordType<T>(private val name: String): SurrealObject<T>(name){
    abstract val id: RecordLink<T, RecordType<T>>
    override operator fun <t, u: ReturnType<t>> TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return if('.' !in thisRef.reference && !thisRef.reference.startsWith('$')) createReference(property.name)
        else createReference("${thisRef.reference}.${property.name}")
    }

}

data class FieldDefinition(val name: String, val type: String, val permissions: List<Permission>)

