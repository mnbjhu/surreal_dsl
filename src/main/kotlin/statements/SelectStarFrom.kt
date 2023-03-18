package statements

import Statement
import core.Table
import types.SurrealArray
import types.ReturnType

class SelectStarFrom<T, U: ReturnType<T>>(private val selector: SurrealArray<T, U>, private val filter: String = ""): Statement(){
    override fun getQueryString(): String {
        return "SELECT * FROM ${wrappedTarget()} $filter"
    }
    private fun wrappedTarget(): String {
        return if(selector is Table<T, *> || selector.reference.startsWith('$')) selector.reference
            else "(${selector.reference})"
    }
}


