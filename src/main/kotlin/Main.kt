import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

val stringType = TypeProducer(StringType("dummy"))
suspend fun main(){
    println(user.getDefinition())
    println(user.data.some.reference)
    // println(user.create(User("Testing", "password", MyDataClass("thing1", "thing2", listOf("One", "Two", "Three")))))
    transaction {
        +create(userTable, User(null, "Testing", "pass", MyDataClass("thing1", "thing2", listOf("1", "2", "3"))))
        val selected by selectStarFrom(userTable)
        update(selected){
            it.username setAs "Updated"
            it.data.some setAs "new_thing"
        }
    }.also { println(it) }
}

val client = HttpClient(CIO){
    install(ContentNegotiation){
        json()
    }
}
abstract class Statement {
    abstract fun getQueryString(): String
}

class InLine(val value: ReturnType<*>): Statement(){
    override fun getQueryString(): String {
        return value.reference
    }
}

class Create<T, U: ReturnType<T>>(private val table: U, private val value: T): Statement(){
    override fun getQueryString(): String {
        return "CREATE ${table.reference} CONTENT ${Json.encodeToString(table.serializer, value)}"
    }
}
class Let<T, U: ReturnType<T>>(private val key: String, private val value: U): Statement(){

    val newReference = value.createReference("$" + key) as U
    override fun getQueryString(): String {
        return "LET $$key = (${value.reference})"
    }

}


class SelectStarFrom<T, U: ReturnType<T>>(private val selector: SurrealArray<T, U>): Statement(){
    override fun getQueryString(): String {
        return "SELECT * FROM ${selector.reference}"
    }
}
class TransactionScope{
    internal val statements: MutableList<Statement> = mutableListOf()
    internal val serializers = mutableListOf<KSerializer<*>>()
    fun getQueryString(): String {
        val begin = "BEGIN TRANSACTION;\n"
        val commit = ";\nCOMMIT TRANSACTION;\n"
        return begin + statements.joinToString(";\n"){ it.getQueryString() } + commit
    }

    fun <T, U: ReturnType<T>>selectStarFrom(selector: SurrealArray<T, U>): SurrealArray<T, U>{
        val select = SelectStarFrom(selector)
        return selector.createReference(select.getQueryString()) as SurrealArray<T, U>
    }

    fun <T, U: ReturnType<T>>update(selector: SurrealArray<T, U>, action: SetScope.(U) -> Unit): SurrealArray<T, U>{
        val scope = SetScope()
        scope.action(selector.inner)
        val ref = if(selector is Table) selector.reference else "(${selector.reference})"
        return selector.createReference("UPDATE $ref ${scope.getString()}") as SurrealArray<T, U>
    }

    operator fun <T, U: ReturnType<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U {
        val let = Let(property.name, this)
        statements.add(let)
        serializers.add(ResultSetParser(String.serializer().nullable))
        return let.newReference
    }
    operator fun <T, U: ReturnType<T>>U.unaryPlus(): U{
        statements.add(InLine(this))
        serializers.add(ResultSetParser(serializer))
        return this
    }
    fun <T, U: ReturnType<T>>create(table: Table<T, U>, value: T): SurrealArray<T, U> {
        return table.createReference("CREATE ${table.reference} CONTENT ${Json.encodeToString(table.inner.serializer, value)}") as SurrealArray<T, U>
    }
}

class SetScope {
    private val params = mutableMapOf<ReturnType<*>, ReturnType<*>>()
    fun getString(): String = "SET " + params.entries.joinToString { "${ it.key.reference } = ${ it.value.reference }" }
    infix fun <T>ReturnType<T>.setAs(value: T){
        params[this] = createReference(Json.encodeToString(serializer, value))
    }
    infix fun <T, U: ReturnType<T>>U.setAs(value: U){
        params[this] = value
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
    val serializer = ResultListSerializer(transaction.serializers.toList() as List<ResultSetParser<Any?, KSerializer<Any?>>>)
    val r = surrealJson.decodeFromString(serializer, response.bodyAsText())
        .last() as ResultSet<T>
    return r.result
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

class RecordLink<T, out U: RecordType<T>>(override val reference: String, val inner: U): StringType(reference) {
    val o: U
        get() = inner.createReference("$reference.*") as U
    companion object {
        fun <T, U: RecordType<T>>Table<T, U>.idOf(key: String) = RecordLink<T, U>("$name:$key", inner)
    }
}

@Serializable
data class User(val id: String? = null, val username: String, val password: String, val data: MyDataClass)

class Table<T, U: ReturnType<T>>(val name: String, inner: U): SurrealArray<T, U>(inner, name){
    constructor(name: String, type: TypeProducer<T, U>): this(name, type.createReference("name"))
}
val userType = TypeProducer(UserRecord("dummy"))
val userTable = Table("user", userType)
val user = userType.createReference("user")

fun <T, U: RecordType<T>>recordLink(to: U) = TypeProducer(RecordLink("dummy", to))

class UserRecord(reference: String): RecordType<User>(reference, User.serializer()){
    override val id by recordLink(this)
    val username by stringType
    val password by stringType
    val data by myDataClassType

    override fun createReference(reference: String): ReturnType<User> {
        return UserRecord(reference)
    }
}

class MySurrealDataClass(reference: String): SurrealObject<MyDataClass>(MyDataClass.serializer(), reference){
    val some by stringType
    val other by stringType
    val arrayData by array(stringType)

    override fun createReference(reference: String): ReturnType<MyDataClass> {
        return MySurrealDataClass(reference)
    }
}
val myDataClassType = TypeProducer(MySurrealDataClass("dummy"))


@Serializable
data class MyDataClass(val some: String, val other: String, val arrayData: List<String>)


interface ReturnType<T> {
    val serializer: KSerializer<T>
    val reference: String
    fun createReference(reference: String): ReturnType<T>
    fun getFieldDefinition(tableName: String): String
}

open class StringType(override val reference: String): ReturnType<String> {

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

/*
abstract class Relation<T>(private val name: String, serializer: KSerializer<T>): RecordType<T>(serializer, name){
    protected override operator fun <t, u: ReturnType<t>>TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference(property.name)
    }

    fun getDefinition(): String {
        val fields = this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                        it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) value
                else null
            }
            .joinToString("\n") { it.getFieldDefinition(name) }
        return "DEFINE TABLE $name SCHEMAFULL;\n$fields"
    }
}
*/
abstract class RecordType<T>(private val name: String, serializer: KSerializer<T>): SurrealObject<T>(serializer, name){
    abstract val id: RecordLink<T, RecordType<T>>
    protected override operator fun <t, u: ReturnType<t>>TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference(property.name)
    }

    fun getDefinition(): String {
        val fields = this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                        it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) value
                else null
            }
            .joinToString("\n") { it.getFieldDefinition(name) }
        return "DEFINE TABLE $name SCHEMAFULL;\n$fields"
    }
}

abstract class SurrealObject<T>(override val serializer: KSerializer<T>, override val reference: String): ReturnType<T> {
    override fun getFieldDefinition(tableName: String): String {
        return "DEFINE FIELD $reference.* ON $tableName TYPE object;\n" + this::class.members
            .filter {
                it.returnType.isSubtypeOf(ReturnType::class.createType(listOf(KTypeProjection.STAR))) &&
                    it.parameters.size == 1
            }
            .mapNotNull {
                val value = it.call(this)
                if(value is ReturnType<*>) value
                else null
            }
            .joinToString("\n") { it.getFieldDefinition(tableName) }
    }
    protected open operator fun <t, u: ReturnType<t>>TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u{
        return createReference("${thisRef.reference}.${property.name}") as u
    }

}

open class SurrealArray<T, U: ReturnType<T>>(internal val inner: U, override val reference: String): ReturnType<List<T>> {
    override val serializer: KSerializer<List<T>> = ListSerializer(inner.serializer)
    override fun createReference(reference: String): ReturnType<List<T>> {
        return SurrealArray(inner, reference)
    }

    override fun getFieldDefinition(tableName: String): String {
        return "DEFINE FIELD $reference ON $tableName TYPE array;\n" +
                inner.createReference("$reference.*").getFieldDefinition(tableName)
    }

}

