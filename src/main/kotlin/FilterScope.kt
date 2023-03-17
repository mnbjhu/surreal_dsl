import core.Filter
import statements.OrderBy
import types.BooleanType

class FilterScope() {
    private val filters = mutableListOf<Filter>()
    fun getString(): String{
        return filters.joinToString(" ") { it.getString() }
    }
    fun where(condition: BooleanType){
        filters.add(Where(condition))
    }
    fun orderBy(vararg comparable: SurrealComparable<*>){
        filters.add(OrderBy(comparable.toList()))
    }
}