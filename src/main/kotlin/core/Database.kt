package core

import InLine
import RecordType
import data.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
    suspend fun <a, A: ReturnType<a>, b, B: RecordType<b>>signInToWebsocket(scope: Scope<a, A, b, B>, key: b): DatabaseWebsocketConnection{
        val connection = client.webSocketSession("ws://${nameSpace.server.host}:${nameSpace.server.port}/rpc"){

        }
        connection.send(Frame.Text("{\"id\":\"${IdCounter.next()}\",\"method\":\"signin\",\"params\":[{\"NS\":\"${nameSpace.name}\",\"DB\":\"${name}\",\"SC\":\"${scope.name}\",\"creds\":${surrealJson.encodeToString(scope.signInType.serializer, key)}}]}".also { println(it)}))
        return DatabaseWebsocketConnection(this, connection).also {
            CoroutineScope(Dispatchers.IO).launch { connection.incoming.receiveAsFlow().collect { println((it as Frame.Text).readText()) } }
        }

    }
}

object IdCounter {
    private var id: Long = 1
    fun next() = id++
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

class DatabaseWebsocketConnection(val database: Database, val ws: WebSocketSession){
    private val channels = mutableMapOf<Long, Channel<String>>()
    init {
        CoroutineScope(Dispatchers.IO).launch {
            ws.incoming.receiveAsFlow().collect {
                it as Frame.Text
                val responseText = it.readText()
                println(responseText)
                val match = "^\\{\"id\":\"(\\d+)\"".toRegex().find(responseText) ?: throw Exception("No response id found")
                val id = (match.groups[1] ?: throw Exception("No response id found-")).value.toLong()
                val channel = channels[id] ?: throw Exception("Id doesn't correspond to any that was sent: '$id'")
                channel.send(responseText)
            }
        }
    }
    private suspend fun sendQuery(method: String, params: String): String{
        val id = IdCounter.next()
        val channel = Channel<String>(1)
        channels[id] = channel
        ws.send(Frame.Text("{\"id\":\"$id\",\"method\":\"$method\",\"params\":[\"$params\"]}".also { println(it) }))
        val response = channel.receive()
        channels.remove(id)
        return response
    }


    suspend fun <T, U: ReturnType<T>>transaction(scope: TransactionScope.() -> U): T {
        val transaction = TransactionScope()
        val result =  transaction.scope()
        transaction.serializers.add(ResultSetParser(result.serializer))
        transaction.statements.add(InLine(result))
        val serializers = transaction.serializers.toList()
                as List<ResultSetParser<Any?, KSerializer<Any?>>>
        val serializer = WebSocketResponseSerializer(ResultListSerializer(serializers))
        val response = sendQuery("query", transaction.getQueryString())
        surrealJson.decodeFromString(serializer, response)
        response as WebsocketResponse.Success<List<*>>
        val r = response.result.first() as ResultSet<T>
        return r.result
    }

    suspend fun <T, U: ReturnType<T>>liveTransaction(scope: TransactionScope.() -> U){
        val transaction = TransactionScope()
        val result =  transaction.scope()
        transaction.serializers.add(ResultSetParser(result.serializer))
        transaction.statements.add(InLine(result))
        val serializers = transaction.serializers.toList()
                as List<ResultSetParser<Any?, KSerializer<Any?>>>
        val serializer = WebSocketResponseSerializer(ResultListSerializer(serializers))
        println(sendQuery("live", transaction.getQueryString()))
    }
}