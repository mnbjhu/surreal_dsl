package core

import FilterScope
import InLine
import RecordType
import Statement
import data.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
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
    fun getQueryString(): String {
        val begin = "BEGIN TRANSACTION;\n"
        val commit = ";\nCOMMIT TRANSACTION;\n"
        return begin + statements.joinToString(";\n"){ it.getQueryString() } + commit
    }

    fun <T, U: ReturnType<T>>SurrealArray<T, U>.selectAll(filter: FilterScope.(U) -> Unit = {}): SurrealArray<T, U> {
        val filterText = FilterScope().apply{ filter(inner) }.getString()
        val select = SelectStarFrom(this, filterText)
        return createReference(select.getQueryString()) as SurrealArray<T, U>
    }

    fun <T, U: ReturnType<T>>SurrealArray<T, U>.update(action: SetScope.(U) -> Unit): SurrealArray<T, U> {
        val scope = SetScope()
        scope.action(inner)
        val ref = if(this is Table) reference else "($reference)"
        return createReference("UPDATE $ref ${scope.getString()}") as SurrealArray<T, U>
    }

    fun <T, U: ReturnType<T>>SurrealArray<T, U>.create(action: SetScope.(U) -> Unit): SurrealArray<T, U> {
        val scope = SetScope()
        scope.action(inner)
        val ref = if(this is Table) reference else "($reference)"
        return createReference("CREATE $ref ${scope.getString()}") as SurrealArray<T, U>
    }

    fun <T, U: ReturnType<T>, a, A: ReturnType<a>>SurrealArray<T, U>.select(projection: FilterScope.(U) -> A): SurrealArray<a, A> {
        val scope = FilterScope()
        val result = scope.projection(inner)
        val filterText = scope.getString()
        return array(TypeProducer(Multiple1(result, ""))).createReference("SELECT ${result.reference} AS col1 FROM $reference" +
                (if(filterText == "") "" else " $filterText")) as SurrealArray<a, A>
    }

    operator fun <T, U: ReturnType<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U {
        val let = Let(property.name, this)
        statements.add(let)
        serializers.add(ResultSetParser(String.serializer().nullable))
        return let.newReference
    }
    operator fun <T, U: ReturnType<T>>U.unaryPlus(): U {
        statements.add(InLine(this))
        serializers.add(ResultSetParser(serializer))
        return this
    }
    fun <T, U: RecordType<T>>Table<T, U>.createContent(value: T): SurrealArray<T, U> {
        return createReference("CREATE $reference CONTENT ${Json.encodeToString(inner.serializer, value)}") as SurrealArray<T, U>
    }
}

suspend fun <T, U: ReturnType<T>>transaction(scope: TransactionScope.() -> U): T {
    val transaction = TransactionScope()
    val result =  transaction.scope()
    transaction.serializers.add(ResultSetParser(result.serializer))
    transaction.statements.add(InLine(result))
    val response = client.post("http://localhost:8000/sql"){
        contentType(ContentType.Application.Json)
        basicAuth("root", "root")
        header("ns", "test")
        header("db", "test")
        setBody(transaction.getQueryString().also { println(it) })
    }
    val serializer =
        ResultListSerializer(transaction.serializers.toList() as List<ResultSetParser<Any?, KSerializer<Any?>>>)
    val r = surrealJson.decodeFromString(serializer, response.bodyAsText().also{println(it)})
        .last() as ResultSet<T>
    return r.result
}

data class SurrealServer(val host: String, val port: Int, val auth: Auth) {
    fun namespace(name: String) = NameSpace(this, name)
}


sealed class Auth {
    abstract fun HttpRequestBuilder.authenticate()
    data class Root(val username: String, val password: String): Auth() {
        override fun HttpRequestBuilder.authenticate(){
            basicAuth("root", "root")
        }
    }
}

data class NameSpace(val server: SurrealServer, val name: String) {
    fun database(name: String) = Database(this, name)
}



