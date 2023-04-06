package statements

import core.Filter
import types.BooleanType

class Where(private val condition: BooleanType): Filter(){
    override fun getString(): String {
        return "WHERE ${condition.reference}"
    }
}