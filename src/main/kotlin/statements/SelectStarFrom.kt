package statements

import types.ReturnType

class SelectStarFrom(private val selector: ReturnType<*>, private val filter: String = ""): Statement(){
    override fun getQueryString(): String {
        return "SELECT * FROM ${selector.reference} $filter"

    }
    private fun wrappedTarget(): String {
        return if(selector.reference!!.startsWith('$')) selector.reference!!
            else "(${selector.reference})"
    }
}


