package types

import core.TypeProducer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

abstract class SurrealObject<T>(override val serializer: KSerializer<T>, override val reference: String):
    ReturnType<T> {
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

}