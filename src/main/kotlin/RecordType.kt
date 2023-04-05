import core.Permission
import core.PermissionType
import core.Scope
import core.TypeProducer
import types.*
import kotlin.reflect.KProperty
import kotlin.reflect.KFunction0
import kotlin.reflect.full.primaryConstructor

val recordTypes = mutableMapOf<String, TableSettings>()

class TableSettings {
    val permissions = mutableMapOf<PermissionType, MutableMap<Scope<*, *, *, *, *, *>, BooleanType>>()
    val fieldSettings = mutableMapOf<String, FieldSettings>()
}

class FieldSettings {
    val permissions = mutableMapOf<PermissionType, MutableMap<Scope<*, *, *, *, *, *>, BooleanType>>()
    var assertions = mutableListOf<BooleanType>()
}

abstract class RecordType<T>(val tableName: String): SurrealObject<T>() {
    private val isFirstInstance = tableName !in recordTypes
    abstract val id: RecordLink<T, RecordType<T>>
    override operator fun <t, u: ReturnType<t>> TypeProducer<t, u>.getValue(thisRef: ReturnType<T>, property: KProperty<*>): u {
        if(isFirstInstance) {
            val fieldSettings = recordTypes.getOrPut(tableName){ TableSettings() }.fieldSettings.getOrPut(property.name) { FieldSettings() }
            fieldSettings.assertions = (settings?.assertions ?: mutableListOf())
        }
        return if('.' !in thisRef.reference!! && !thisRef.reference!!.startsWith('$')) createReference(property.name)
        else createReference("${thisRef.reference}.${property.name}")
    }

    override fun createReference(reference: String): ReturnType<T> {
        return this::class.primaryConstructor!!.call().withReference(reference)
    }

    /*
    protected fun <T, U: ReturnType<T>>TypeProducer<T, U>.permissionFor(vararg actions: PermissionType, assertion: (U) -> BooleanType): TypeProducer<T, U>{
        if(isFirstInstance){
            if(settings == null) {
                settings = FieldSettings()

            }
            val inner = createReference("\$value")
            settings!!.assertions.add(assertion(inner))

        }
        return this
    }

     */
    protected fun <T, U: ReturnType<T>>TypeProducer<T, U>.assert(assertion: (U) -> BooleanType): TypeProducer<T, U> {
        if(isFirstInstance){
            if(settings == null) {
                settings = FieldSettings().apply { assertions.add(BooleanType.createReference("\$value != NONE")) }
            }
            val inner = createReference("\$value")
            return TypeProducer(inner).also {
                it.settings = FieldSettings()
                it.settings!!.assertions.addAll(settings?.assertions ?: listOf())
                it.settings!!.assertions.add(assertion(inner))
                it.settings!!.permissions.putAll(settings?.permissions ?: mapOf())
            }
        }
        return this
    }
}

data class FieldDefinition(val name: String, val type: String, val permissions: List<Permission>) {

}
fun KFunction0<RecordType<*>>.getDefinition(): String {
    val inner = call().apply { withReference(tableName) }
    val fieldTypeBounds = inner.getFieldTypeBounds()
    val settings = recordTypes[inner.tableName]
    return "DEFINE TABLE ${inner.tableName} SCHEMAFULL" +
            (settings ?: TableSettings()).permissions.entries.joinToString { permission ->
                "PERMISSIONS FOR ${permission.key} WHERE IF ${permission.value.entries.joinToString(" ELSE IF ") { "\$scope == \"${it.key.name}\" THEN ${it.value.reference} " }} ELSE FALSE"
            } +
            ";\n" + fieldTypeBounds.entries
        .mapIndexedNotNull { index, it ->
            if(it.key != ""){
                val assertions = (settings
                    ?.fieldSettings
                    ?.get(it.key)
                    ?.assertions ?:  listOf(BooleanType.createReference("\$value != NONE")) )
                    .joinToString(separator = " AND "){ "(${it.reference})" }
                "DEFINE FIELD ${it.key} ON ${inner.tableName} TYPE ${it.value}" + if (assertions?.isNotEmpty() == true) " ASSERT $assertions" else ""
            } else null
        }.joinToString(";\n", postfix = ";\n")
}
