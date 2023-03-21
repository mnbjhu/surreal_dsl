package core

import InLine
import RecordType
import data.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import types.ReturnType

data class Database(val nameSpace: NameSpace, val name: String){
    fun connectAsAdmin(username: String, password: String) = DatabaseConnection(this, Auth.Root(username, password))

    suspend fun <a, A: ReturnType<a>, b, B: RecordType<b>>signIn( scope: Scope<a, A, b, B>, key: b): DatabaseConnection{
        val response = client.post("http://${nameSpace.server.host}:${nameSpace.server.port}/signin"){
            contentType(ContentType.Application.Json)
            setBody("{\"ns\": \"${nameSpace.name}\",\"db\": \"${name}\",\"sc\": \"${scope.name}\", \"creds\":" + Json.encodeToString(scope.signInType.serializer, key)  + "}")
        }
        if(response.status != HttpStatusCode.OK) throw Exception("Failed to sign-in ${response.status} '${response.bodyAsText()}'")
        val authData = response.body<AuthResponse>()
        return DatabaseConnection(this, Auth.Session(authData.token))
    }
    suspend fun <a, A: ReturnType<a>, b, B: RecordType<b>>signup(scope: Scope<a, A, b, B>, key: a): DatabaseConnection {
        val response = client.post("http://${nameSpace.server.host}:${nameSpace.server.port}/signup"){
            contentType(ContentType.Application.Json)
            setBody("{\"ns\": \"${nameSpace.name}\",\"db\": \"${name}\",\"sc\": \"${scope.name}\", \"creds\":" + Json.encodeToString(scope.signupType.serializer, key)  + "}")
        }
        if(response.status != HttpStatusCode.OK) throw Exception("Failed to sign-up ${response.status} ${response.bodyAsText()}")
        val authData = response.body<AuthResponse>()
        return DatabaseConnection(this, Auth.Session(authData.token))
    }
}

@Serializable
data class AuthResponse(val code: Int, val token: String, val details: String)
class DatabaseConnection(val database: Database, val auth: Auth){
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
        sendQuery("BEGIN TRANSACTION; ${schema.tables.joinToString("\n") { it.getDefinition() }} ${schema.scopes.joinToString(";\n") { it.getDefinition() }};  COMMIT TRANSACTION;")
    }

    private suspend fun sendQuery(query: String) = client.post("http://localhost:8000/sql"){
        contentType(ContentType.Application.Json)
        with(auth) { authenticate() }
        header("ns", database.nameSpace.name)
        header("db", database.name)
        setBody(query)
    }

    suspend fun remove() {
        sendQuery("REMOVE DATABASE ${database.name}")
    }

    suspend fun define() {
        sendQuery("DEFINE DATABASE ${database.name}")
    }
}