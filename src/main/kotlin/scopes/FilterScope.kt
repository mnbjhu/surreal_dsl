package scopes

import core.SurrealComparable
import types.Table
import core.Filter
import core.Linked
import statements.OrderBy
import statements.Where
import types.BooleanType
import types.RecordLink
import types.ReturnType
import types.SurrealArray

class FilterScope() {
    private val filters = mutableListOf<Filter>()
    private val toFetch = mutableListOf<ReturnType<*>>()
    fun getString(): String{
        return filters.joinToString(" ") { it.getString() } + if(toFetch.isNotEmpty()) " FETCH ${toFetch.joinToString { it.reference!! }}" else ""
    }
    fun where(condition: BooleanType){
        filters.add(Where(condition))
    }
    fun orderBy(vararg comparable: SurrealComparable<*>){
        filters.add(OrderBy(comparable.toList()))
    }

    fun <T, U: Table<T>>fetch(linked: RecordLink<T, U>){
        toFetch.add(linked)
    }

    fun <T, U: Table<T>>fetch(linked: SurrealArray<Linked<T>, RecordLink<T, U>>){
        toFetch.add(linked)
    }
}