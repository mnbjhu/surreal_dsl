package statements

import Statement
import kotlinx.serialization.json.Json
import types.ReturnType

class Create<T, U: ReturnType<T>>(private val table: U, private val value: T): Statement(){
    override fun getQueryString(): String {
        return "CREATE ${table.reference} CONTENT ${Json.encodeToString(table.serializer, value)}"
    }
}