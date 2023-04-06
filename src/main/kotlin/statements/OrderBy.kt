package statements

import core.Filter
import core.SurrealComparable

class OrderBy(private val by: List<SurrealComparable<*>>): Filter(){
    override fun getString(): String {
        return "ORDER BY ${ by.joinToString { it.reference!! } }"
    }
}