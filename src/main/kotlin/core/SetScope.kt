package core

import RecordType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import types.RecordLink
import types.ReturnType
import types.SurrealArray

class SetScope {
    private val params = mutableMapOf<ReturnType<*>, ReturnType<*>>()
    fun getString(): String = "SET " + params.entries.joinToString { "${ it.key.reference } = ${ if(it.value is SurrealArray<*, *>) "(${it.value.reference})" else it.value.reference }" }
    infix fun <T> ReturnType<T>.setAs(value: T){
        params[this] = createReference(Json.encodeToString(serializer, value))
    }
    infix fun <T, U: ReturnType<T>>U.setAs(value: U) {
        params[this] = value
    }

    /*
    infix fun <T, U: RecordType<T>> SurrealArray<String, RecordLink<T, U>>.setAs(value: SurrealArray<T, U>){
        params[this] = value
    }
     */
    infix fun <T, U: RecordType<T>> SurrealArray<Linked<T>, RecordLink<T, U>>.setAs(value: List<RecordLink<T, U>>){
        val ref = "[${value.joinToString { it.reference }}]"
        params[this] = createReference(ref)
    }

    operator fun <T, U: ReturnType<T>>U.invoke(innerScope: U.() -> Unit): Unit{
        innerScope()
    }
}