package statements

import types.ReturnType

class Let<T, U: ReturnType<T>>(private val key: String, private val value: U): Statement(){

    val newReference = value.createReference("$$key") as U
    override fun getQueryString(): String {
        return "LET $$key = (${value.reference})"
    }

}