package core

import scopes.TransactionScope
import types.ReturnType
import types.SurrealArray
import kotlin.time.Duration


abstract class Scope<a, A: ReturnType<a>, b, B: ReturnType<b>, c, C: ReturnType<c>> {
    abstract val name: String
    abstract val signupType: A
    abstract val signInType: B
    abstract val tokenType: C
    abstract val sessionDuration: Duration

    abstract fun TransactionScope.signup(credentials: A): SurrealArray<c, C>

    abstract fun TransactionScope.signIn(credentials: B): SurrealArray<c, C>

    fun getDefinition(): String {
        val transaction = TransactionScope()
        return with(transaction){"DEFINE SCOPE $name SESSION $sessionDuration \n" +
                "   SIGNUP (${signup(signupType.createReference("\$creds") as A).reference})\n" +
                "   SIGNIN (${signIn(signInType.createReference("\$creds") as B).reference})"}
    }
}

/*
class PermissionScope {


    val tablePermissions = mutableListOf<Permission>()

    val fieldPermissions = mutableMapOf<String, Permission>()

    fun <T, U: ReturnType<T>>U.none(){
        fieldPermissions[reference!!] = Permission.None
    }
    fun <T, U: ReturnType<T>>U.full(){
        fieldPermissions[reference!!] = Permission.Full
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

 */

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