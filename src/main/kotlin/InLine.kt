import types.ReturnType

class InLine(private val value: ReturnType<*>): Statement(){
    override fun getQueryString(): String {
        return value.reference
    }
}