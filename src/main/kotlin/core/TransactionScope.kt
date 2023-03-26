package core

import FilterScope
import InLine
import RecordType
import Statement
import data.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import statements.Let
import statements.SelectStarFrom
import types.ReturnType
import types.SurrealArray
import types.array
import types.multiple.Multiple1
import kotlin.reflect.KProperty

class TransactionScope{
    internal val statements: MutableList<Statement> = mutableListOf()
    internal val serializers = mutableListOf<KSerializer<*>>()
    internal val variables = mutableMapOf<String, ReturnType<*>>()
    fun getQueryString(): String {
        val begin = "BEGIN TRANSACTION;\n"
        val commit = ";\nCOMMIT TRANSACTION;\n"
        return begin + statements.joinToString(";\n"){ it.getQueryString() } + commit
    }


    fun <T, U: ReturnType<T>>SurrealArray<T, U>.selectAll(filter: context(FilterScope) U.() -> Unit = {}): SurrealArray<T, U> {
        val filterText = FilterScope().apply{ filter(inner) }.getString()
        val select = SelectStarFrom(this, filterText)
        return createReference(select.getQueryString())
    }

    fun <T, U: ReturnType<T>>SurrealArray<T, U>.update(action: context(SetScope) U.() -> Unit): SurrealArray<T, U> {
        val scope = SetScope()
        scope.action(inner)
        val ref = if(this is Table) reference else "($reference)"
        return createReference("UPDATE $ref ${scope.getString()}")
    }

    fun <T, U: ReturnType<T>>SurrealArray<T, U>.create(action: context(SetScope) U.() -> Unit): SurrealArray<T, U> {
        val scope = SetScope()
        scope.action(inner)
        val ref = if(this is Table) reference else "($reference)"
        return createReference("CREATE $ref ${scope.getString()}")
    }

    fun <T, U: ReturnType<T>, a, A: ReturnType<a>>SurrealArray<T, U>.select(projection: context(FilterScope) U.() -> A): SurrealArray<a, A> {
        val scope = FilterScope()
        val result = scope.projection(inner)
        val filterText = scope.getString()
        val wrappedReference = if(this is SurrealArray) reference else "($reference)"
        return array(TypeProducer(Multiple1(result, ""))).createReference("SELECT ${result.reference} AS col1 FROM $wrappedReference" +
                (if(filterText == "") "" else " $filterText")) as SurrealArray<a, A>
    }

    operator fun <T, U: ReturnType<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U {
        return variables.getOrPut(property.name){
            val let = Let(property.name, this)
            statements.add(let)
            serializers.add(ResultSetParser(String.serializer().nullable))
            let.newReference
        } as U
    }
    operator fun <T, U: ReturnType<T>>U.unaryPlus(): U {
        statements.add(InLine(this))
        serializers.add(ResultSetParser(serializer))
        return this
    }
    fun <T, U: RecordType<T>>Table<T, U>.createContent(value: T): SurrealArray<T, U> {
        return createReference("CREATE $reference CONTENT ${Json.encodeToString(inner.serializer, value)}")
    }
}


data class SurrealServer(val host: String, val port: Int) {
    fun namespace(name: String) = NameSpace(this, name)

    companion object {
    }
}


sealed class Auth {
    abstract fun HttpRequestBuilder.authenticate()
    data class Root(val username: String, val password: String): Auth() {
        override fun HttpRequestBuilder.authenticate(){
            basicAuth("root", "root")
        }
    }
    class Session(private val token: String): Auth() {
        override fun HttpRequestBuilder.authenticate() {
            bearerAuth(token)
        }
    }
}

data class NameSpace(val server: SurrealServer, val name: String) {
    fun database(name: String) = Database(this, name)
}


