package core

import RecordType
import types.BooleanType
import types.ReturnType
import types.SurrealArray
import kotlin.reflect.KProperty
import kotlin.time.Duration

open class Table<T, U: RecordType<T>>(val name: String, inner: U): SurrealArray<T, U>(inner, name){
    constructor(name: String, type: TypeProducer<T, U>): this(name, type.createReference(name))
    fun getDefinition(): String {
        val fieldTypeBounds = inner.getFieldTypeBounds()
        val permissionScope = PermissionScope()
        permissionScope.permissions(inner)
        return "DEFINE TABLE $name SCHEMAFULL" + permissionScope.tablePermissions.joinToString(prefix = " ") { it.getString() } + ";\n" + fieldTypeBounds.entries
            .mapIndexedNotNull { index, it ->
            if(it.key != ""){
                if(index == 1){
                    "DEFINE FIELD ${it.key} ON $name TYPE ${it.value}" + (permissionScope.fieldPermissions[it.key]?.getString() ?: "")
                } else {
                    "DEFINE FIELD ${it.key} ON $name TYPE ${it.value}"
                }
            } else null
        }.joinToString(";\n", postfix = ";\n")
    }
    open fun PermissionScope.permissions(record: U){}
}

abstract class Scope<a, A: ReturnType<a>, b, B: RecordType<b>> {
    abstract val name: String
    abstract val signupType: A
    abstract val signInType: B
    abstract val sessionDuration: Duration

    abstract fun TransactionScope.signup(credentials: A): SurrealArray<b, B>

    abstract fun TransactionScope.signIn(credentials: B): SurrealArray<b, B>

    fun getDefinition(): String{
        val transaction = TransactionScope()
        return with(transaction){"DEFINE SCOPE $name SESSION $sessionDuration SIGNUP (${signup(signupType.createReference("\$creds") as A).reference}) SIGNIN (${signIn(signInType.createReference("\$creds") as B).reference})"}
    }
}

class PermissionScope {


    val tablePermissions = mutableListOf<Permission>()

    val fieldPermissions = mutableMapOf<String, Permission>()

    fun <T, U: ReturnType<T>>U.none(){
        fieldPermissions[reference] = Permission.None
    }
    fun <T, U: ReturnType<T>>U.full(){
        fieldPermissions[reference] = Permission.Full
    }
    fun none(){
        tablePermissions.add(Permission.None)
    }
    fun full(){
        tablePermissions.add(Permission.Full)
    }
    fun permissionsFor(type: PermissionType, permissionBuilder: PermissionBuilder.() -> Unit){
        val builder = PermissionBuilder()
        builder.permissionBuilder()
        tablePermissions.add(Permission.For(type, builder.getExpression()))
    }
}

class PermissionBuilder(){
    private val typeBranch = mutableMapOf<Scope<*, *, *, *>, BooleanType>()
    fun <T, U: RecordType<T>>scope(scope: Scope<*, *, T, U>, where: (U) -> BooleanType){
        typeBranch[scope] = where(scope.signInType.createReference("\$auth") as U)
    }
    fun getExpression(): String{
        return typeBranch.entries.joinToString(" ELSE IF ", "IF ", " END") { "\$scope = \"${it.key.name}\" THEN ${it.value.reference}" }
    }
}

sealed class Permission {

    abstract fun getString(): String

    object Full: Permission() {
        override fun getString(): String = " PERMISSIONS FULL"
    }

    object None: Permission() {
        override fun getString(): String = " PERMISSIONS NONE"
    }

    data class For(val action: PermissionType, val expression: String): Permission() {
        override fun getString(): String = " PERMISSIONS FOR ${action.text} WHERE ($expression)"
    }
}
enum class PermissionType(val text: String) {
    Select("select"),
    Create("create"),
    Update("update"),
    Delete("delete")
}