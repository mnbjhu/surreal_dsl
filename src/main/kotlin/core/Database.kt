package core

import InLine
import data.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import types.ReturnType

data class Database(val nameSpace: NameSpace, val name: String){
    suspend fun <T, U: ReturnType<T>>transaction(scope: TransactionScope.() -> U): T {
        val transaction = TransactionScope()
        val result =  transaction.scope()
        transaction.serializers.add(ResultSetParser(result.serializer))
        transaction.statements.add(InLine(result))
        val response = sendQuery(transaction.getQueryString().also { println(it) })
        val serializers = transaction.serializers.toList()
                as List<ResultSetParser<Any?, KSerializer<Any?>>>
        val serializer = ResultListSerializer(serializers)
        val r = surrealJson
            .decodeFromString(serializer, response.bodyAsText().also{println(it)})
            .last() as ResultSet<T>
        return r.result
    }

    suspend fun setSchema(schema: SurrealSchema){
        sendQuery("BEGIN TRANSACTION; ${schema.tables.joinToString("\n") { it.getDefinition() }} COMMIT TRANSACTION;")
    }

    private suspend fun sendQuery(query: String) = client.post("http://localhost:8000/sql"){
        contentType(ContentType.Application.Json)
        with(nameSpace.server.auth) { authenticate() }
        header("ns", nameSpace.name)
        header("db", name)
        setBody(query)
    }

    suspend fun remove() {
        sendQuery("REMOVE DATABASE $name")
    }

    suspend fun define() {
        sendQuery("DEFINE DATABASE $name")
    }
}