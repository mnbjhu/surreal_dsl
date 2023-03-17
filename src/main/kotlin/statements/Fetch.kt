package statements

import Statement
import types.ReturnType
import types.SurrealArray

class Fetch(val from: SurrealArray<*, *>, val to: ReturnType<*>): Statement(){
    override fun getQueryString(): String {
        return "SELECT ${from.reference} FETCH ${to.reference}"
    }

}