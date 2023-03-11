import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import javax.lang.model.type.ReferenceType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/*
interface Reference<T>{


    fun getReference(): String
}

interface DataType<T, U: Reference<T>> {
    fun createReference(reference: String): U
}

interface Projection<f, F: Reference<f>, t, T: Reference<t>> {

}

class Select<f, F: Reference<f>, t, T: Reference<t>>

class From<f, F: Reference<f>, t, T: Reference<t>>

fun <f, F: Reference<f>, t, T: Reference<t>>select(projection: Projection<t, T, f, F>): Select<t, T, f, F> {

}

fun <f, F: Reference<f>, t, T: Reference<t>>From<f, F, t, T>.from(){

}

 */

fun main(){
    println(UserTable.getDefinition())
}

@Serializable
data class User(val username: String, val password: String)

object UserTable: Table<User>("user", User.serializer()){
    val username by StringType()
    val password by StringType()
}


interface ReturnType<out T> {
    val serializer: KSerializer<out T>
    val reference: String?
    fun getFieldDefinition(tableName: String, fieldName: String): String
}

class StringType(override val reference: String? = null): ReturnType<String> {
    override val serializer: KSerializer<String> = String.serializer()

    override fun getFieldDefinition(tableName: String, fieldName: String): String =
        "DEFINE FIELD $fieldName ON $tableName TYPE string"
}

abstract class Table<out T>(private val name: String, serializer: KSerializer<T>): SurrealObject<T>(serializer){
    fun getDefinition(): String {
        return "DEFINE TABLE $name SCHEMAFULL\n" + columns.entries.joinToString { it.value.getFieldDefinition(name, it.key) }
    }
}

abstract class SurrealObject<out T>(override val serializer: KSerializer<out T>,
                              override val reference: String? = null): ReturnType<T> {
    protected val columns = mutableMapOf<String, ReturnType<Any?>>()
    override fun getFieldDefinition(tableName: String, fieldName: String): String {
        return columns.entries.joinToString{ it.value.getFieldDefinition(tableName, "$fieldName.${it.key}") }
    }
    protected operator fun <T, U: ReturnType<T>>U.getValue(thisRef: SurrealObject<T>, property: KProperty<*>): U {
        columns[property.name] = this
        return this
    }
}