import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty

val stringType = TypeProducer(StringType("dummy"))
suspend fun main(){
    println(user.getDefinition())
    println(user.data.some.reference)
    // println(user.create(User("Testing", "password", MyDataClass("thing1", "thing2", listOf("One", "Two", "Three")))))
    transaction { selectStarFrom(userTable) }
}

val client = HttpClient(CIO){
    install(ContentNegotiation){
        json()
    }
}
abstract class Statement {
    abstract fun getQueryString(): String
}


class Create<T, U: ReturnType<T>>(private val table: U, private val value: T): Statement(){
    override fun getQueryString(): String {
        return "CREATE ${table.reference} CONTENT ${Json.encodeToString(table.serializer, value)}"
    }
}
class Let<T, U: ReturnType<T>>(private val key: String, private val value: U): Statement(){

    val newReference = value.createReference(key) as U
    override fun getQueryString(): String {
        return "LET $$key = (${value.reference})"
    }

}


class SelectStarFrom<T, U: ReturnType<T>>(private val selector: SurrealArray<T, U>): Statement(){
    override fun getQueryString(): String {
        return "SELECT * FROM ${selector.reference}"
    }
}
class TransactionScope(){
    internal val statements: MutableList<Statement> = mutableListOf()
    fun getQueryString(): String {
        val begin = "BEGIN TRANSACTION;\n"
        val commit = ";\nCOMMIT TRANSACTION;\n"
        return begin + statements.joinToString(";\n"){ it.getQueryString() } + commit
    }

    fun <T, U: ReturnType<T>>selectStarFrom(selector: SurrealArray<T, U>): SurrealArray<T, U>{
        val select = SelectStarFrom(selector)
        statements.add(select)
        return selector.createReference(select.getQueryString()) as SurrealArray<T, U>
    }

    operator fun <T, U: ReturnType<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U {
        val let = Let(property.name, this)
        statements.add(let)
        return let.newReference
    }
}

suspend fun <T, U: ReturnType<T>>transaction(scope: TransactionScope.() -> U): T{
    val transaction = TransactionScope().apply {}
    val result =  transaction.scope()
    val response = client.post("http://localhost:8000/sql"){
        contentType(ContentType.Application.Json)
        basicAuth("root", "root")
        header("ns", "test")
        header("db", "test")
        setBody(transaction.getQueryString().also{println(it)})
    }
    val serializer = ListSerializer(ResultSetParser(result.serializer))
    return surrealJson.decodeFromString(serializer, response.bodyAsText())
        .first().result
}

private val surrealJson = Json { ignoreUnknownKeys = true }

suspend fun <T, U: RecordType<T>>U.create(value: T): T{
    val query = "CREATE ${this.reference} CONTENT ${ Json.encodeToString(serializer, value) }"
    val response = client.post("http://localhost:8000/sql"){
        contentType(ContentType.Application.Json)
        basicAuth("root", "root")
        header("ns", "test")
        header("db", "test")
        setBody(query)
    }
    return surrealJson.decodeFromString(ListSerializer(ResultSetParser(serializer)), response.bodyAsText())
        .first().result
}

@Serializable
data class User(val username: String, val password: String, val data: MyDataClass)

val userType = TypeProducer(UserRecord("dummy"))
val userTable = array(userType).createReference("user")
val user = userType.createReference("user")

class UserRecord(reference: String): RecordType<User>(reference, User.serializer()){
    val username by stringType
    val password by stringType
    val data by myDataClassType
    override val columns: Map<String, ReturnType<*>>
        get() = listOf(
            "username" to username,
            "password" to password,
            "data" to data
        ).toMap()

    override fun createReference(reference: String): ReturnType<User> {
        return UserRecord(reference)
    }
}

class MySurrealDataClass(reference: String): SurrealObject<MyDataClass>(MyDataClass.serializer(), reference){
    val some by stringType
    val other by stringType
    val arrayData by array(stringType)
    override val columns: Map<String, ReturnType<*>>
        get() = listOf(
            "some" to some,
            "other" to other,
            "arrayData" to arrayData,
        ).toMap()

    override fun createReference(reference: String): ReturnType<MyDataClass> {
        return MySurrealDataClass(reference)
    }
}
val myDataClassType = TypeProducer(MySurrealDataClass("dummy"))


@Serializable
class MyDataClass(val some: String, val other: String, val arrayData: List<String>)


interface ReturnType<T> {
    val serializer: KSerializer<T>
    val reference: String
    fun createReference(reference: String): ReturnType<T>
    fun getFieldDefinition(tableName: String): String
}

class StringType(override val reference: String): ReturnType<String> {

    override val serializer: KSerializer<String> = String.serializer()

    override fun createReference(reference: String): ReturnType<String> {
        return StringType(reference)
    }

    override fun getFieldDefinition(tableName: String): String =
        "DEFINE FIELD $reference ON $tableName TYPE string;"
}

fun <T, U: ReturnType<T>>array(type: TypeProducer<T, U>): TypeProducer<List<T>, SurrealArray<T, U>>{
    return TypeProducer(SurrealArray(type.createReference("dummy"), "dummy"))
}
class TypeProducer<T, U: ReturnType<T>>(private val dummy: U){
    fun createReference(reference: String): U = dummy.createReference(reference) as U
}

abstract class RecordType<T>(private val name: String, serializer: KSerializer<T>): SurrealObject<T>(serializer, name){
    protected override operator fun <t, u: ReturnType<t>>TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference(property.name)
    }

    fun getDefinition(): String {
        return "DEFINE TABLE $name SCHEMAFULL;\n" + columns.entries.joinToString("\n") { it.value.getFieldDefinition(name) }
    }
}

abstract class SurrealObject<T>(override val serializer: KSerializer<T>, override val reference: String): ReturnType<T> {
    protected abstract val columns: Map<String, ReturnType<*>>
    override fun getFieldDefinition(tableName: String): String {
        return "DEFINE FIELD $reference.* ON $tableName TYPE object;\n" + columns.entries.joinToString("\n") { it.value.getFieldDefinition(tableName) }
    }

    protected open operator fun <t, u: ReturnType<t>>TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference("${thisRef.reference}.${property.name}") as u
    }

}

class SurrealArray<T, U: ReturnType<T>>(private val inner: U, override val reference: String): ReturnType<List<T>> {
    override val serializer: KSerializer<List<T>> = ListSerializer(inner.serializer)
    override fun createReference(reference: String): ReturnType<List<T>> {
        return SurrealArray(inner, reference)
    }

    override fun getFieldDefinition(tableName: String): String {
        return "DEFINE FIELD $reference ON $tableName TYPE array;\n" +
                inner.createReference("$reference.*").getFieldDefinition(tableName)
    }

}

