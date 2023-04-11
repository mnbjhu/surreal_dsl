package core

import data.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import scopes.TransactionScope
import serialization.RpcResponseDeserializer
import statements.InLine
import types.ReturnType

class DatabaseWebsocketConnection(val database: Database, private val ws: WebSocketSession){
    private val channels = ConcurrentMap<Long, Channel<JsonElement>>()
    init {
        CoroutineScope(Dispatchers.IO).launch {
            ws.incoming.receiveAsFlow().collect {
                it as Frame.Text
                val response = Json.decodeFromString(RpcResponseDeserializer, it.readText().also { println(it) })
                val channel = channels[response.id.toLong()] ?: throw Exception("Id doesn't correspond to any that was sent: '${response.id}'")
                when(response) {
                    is RpcResponse.Success -> channel.send(response.result)
                    is RpcResponse.Error -> throw Exception("SurrealDB returned an error (code: ${response.error.code}): '${response.error.message}'")
                }
            }
        }
    }
    private suspend fun sendQuery(method: String, params: List<JsonElement>): JsonElement {
        val id = IdCounter.next()
        val channel = Channel<JsonElement>(1)
        channels[id] = channel
        ws.send(Frame.Text(Json.encodeToString(RpcRequest.serializer(), RpcRequest(id.toString(), method, params)).also { println(it) }))
        val response = channel.receive()
        channels.remove(id)
        return response
    }
    suspend fun <T, U: ReturnType<T>>transaction(scope: TransactionScope.() -> U): T {
        val transaction = TransactionScope()
        val result =  transaction.scope()
        transaction.statements.add(InLine(result))
        val serializer = result.serializer
        val response = sendQuery("query", listOf(Json.encodeToJsonElement(transaction.getQueryString())))
        return surrealJson.decodeFromJsonElement(serializer, (response as JsonArray).last())
    }

    suspend fun <b, B: ReturnType<b>>signin(scope: Scope<*, *, b, B, *, *>, key: b){
       sendQuery("signin", listOf(buildJsonObject {
           put("ns", database.nameSpace.name)
           put("db", database.name)
           put("sc", scope.name)
           put("creds", surrealJson.encodeToJsonElement(scope.signInType.createReference("creds").serializer, key))
       }))
    }
    suspend fun <a, A: ReturnType<a>>signUp(scope: Scope<a, A, *, *, *, *>, key: a){
        sendQuery("signup", listOf(buildJsonObject {
            put("ns", database.nameSpace.name)
            put("db", database.name)
            put("sc", scope.name)
            put("creds", surrealJson.encodeToJsonElement(scope.s ignupType.createReference("creds").serializer, key))
        }))
    }
    suspend fun <T, U: ReturnType<T>>liveTransaction(scope: TransactionScope.() -> U){
        val transaction = TransactionScope()
        val result =  transaction.scope()
        transaction.serializers.add(ResultSetParser(result.serializer))
        transaction.statements.add(InLine(result))
        val serializers = transaction.serializers.toList()
                as List<ResultSetParser<Any?, KSerializer<Any?>>>
        val serializer = WebSocketResponseSerializer(ResultListSerializer(serializers))
        TODO("Waiting for live queries to be released")
    }


}

@Serializable
data class RpcRequest(val id: String, val method: String, val params: List<JsonElement>)

@Serializable
sealed class RpcResponse {

    abstract val id: String
    data class Success(override val id: String, val result: JsonElement): RpcResponse()
    data class Error(override val id: String, val error: core.Error): RpcResponse()
}

@Serializable
data class Error(val code: Long, val message: String)
