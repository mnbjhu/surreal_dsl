package statements

import Statement
import types.SurrealArray
import types.ReturnType

class SelectStarFrom<T, U: ReturnType<T>>(private val selector: SurrealArray<T, U>, private val filter: String = ""): Statement(){
    override fun getQueryString(): String {
        return "SELECT * FROM ${selector.reference} $filter"
    }
}


